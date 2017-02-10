package at.sheldor5.tr.core.persistence;

import at.sheldor5.tr.api.RecordEngine;
import at.sheldor5.tr.api.objects.Day;
import at.sheldor5.tr.api.objects.Month;
import at.sheldor5.tr.api.objects.Record;
import at.sheldor5.tr.api.objects.RecordType;
import at.sheldor5.tr.api.objects.Session;
import at.sheldor5.tr.api.objects.User;
import at.sheldor5.tr.api.objects.Year;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseEngine implements RecordEngine {

  private static final Logger LOGGER = Logger.getLogger(DatabaseEngine.class.getName());

  private static final String INSERT_RECORD = "INSERT INTO [records] ([user_id], [date], [time], [type]) VALUES (?, ?, ?, ?)";
  private static final String SELECT_RECORD = "SELECT * FROM [records] WHERE [pk_record_id] = ? AND [user_id] = ?";

  private static final String SELECT_RECORDS_OF_DAY = "SELECT * FROM [records] WHERE [user_id] = ? AND [date] = ?";
  private static final String SELECT_RECORDS_OF_MONTH = "SELECT * FROM [records] WHERE [user_id] = ? AND [date] >= ? AND [date] <= ?";
  private static final String SELECT_RECORDS_OF_YEAR = "SELECT * FROM [records] WHERE [user_id] = ? AND [date] >= ? AND [date] <= ?";

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private final Connection connection;

  public DatabaseEngine(final Connection connection) {
    this.connection = connection;
  }

  @Override
  public void addRecord(final User user, final Record record) {
    final PreparedStatement statement;
    try {
      statement = connection.prepareStatement(
              INSERT_RECORD,
              Statement.RETURN_GENERATED_KEYS);

      statement.setBytes(1, user.getUUIDBytes());
      statement.setDate(2, Date.valueOf(record.getDate()));
      statement.setTime(3, Time.valueOf(record.getTime()));
      statement.setBoolean(4, record.getType().getBoolean());

      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine(String.format("Adding record (%s %s, %s) for user ID %s",
                record.getDate(),
                record.getTime(),
                record.getType(),
                user.getUUID()));
      }

      if (statement.executeUpdate() != 1) {
        statement.close();
        connection.rollback();
        throw new SQLException("Could not store record: " + statement.toString());
      }

      int recordId;
      try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          recordId = generatedKeys.getInt(1);
        } else {
          statement.close();
          connection.rollback();
          throw new SQLException("Storing record failed, no ID obtained");
        }
      }

      if (!connection.getAutoCommit()) {
        connection.commit();
      }
      statement.close();
      record.setId(recordId);
    } catch (final SQLException sqle) {
      LOGGER.severe(sqle.getMessage());
    }
  }

  @Override
  public void updateRecord(User user, int id, Record newValues) {

  }

  @Override
  public void deleteRecord(User user, int id) {

  }

  @Override
  public Record getRecord(User user, int id) {
    final PreparedStatement statement;
    try {
      statement = connection.prepareStatement(SELECT_RECORD);

      statement.setInt(1, id);

      statement.setBytes(2, user.getUUIDBytes());

      LOGGER.fine("Searching for record with id " + id);

      final ResultSet result = statement.executeQuery();

      final Record record = new Record();

      if (result.next()) {
        record.setId(result.getInt("pk_record_id"));
        record.setDate(result.getDate("date").toLocalDate());
        record.setTime(result.getTime("time").toLocalTime());
        record.setType(RecordType.getType(result.getBoolean("type")));

        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Found record (%s %s, %s) with ID %s",
                  record.getDate(),
                  record.getTime(),
                  record.getType(),
                  record.getId()));
        }
      } else {
        LOGGER.fine("Record with id " + id + " was not found");
        result.close();
        statement.close();
        return null;
      }

      if (result.next()) {
        // record not unique
        result.close();
        statement.close();
        throw new SQLException("Multiple records found!");
      }
      result.close();
      statement.close();
      return record;
    } catch (final SQLException sqle) {
      LOGGER.severe(sqle.getMessage());
    }
    return null;
  }

  @Override
  public List<Record> getDayRecords(final User user, int yyyy, int mm, int dd) {
    final LocalDate date = LocalDate.of(yyyy, mm, dd);
    final PreparedStatement statement;
    try {
      statement = connection.prepareStatement(SELECT_RECORDS_OF_DAY);

      statement.setBytes(1, user.getUUIDBytes());
      statement.setDate(2, Date.valueOf(LocalDate.of(yyyy, mm, dd)));

      LOGGER.fine("Searching for records from " + date);

      final ResultSet result = statement.executeQuery();
      final List<Record> list = new ArrayList<>();

      if (result.next()) {
        do {
          final Record record = new Record();
          record.setId(result.getInt("pk_record_id"));
          record.setDate(result.getDate("date").toLocalDate());
          record.setTime(result.getTime("time").toLocalTime());
          record.setType(RecordType.getType(result.getBoolean("type")));
          list.add(record);

          if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("Found record (%s %s, %s) with ID %s",
                    record.getDate(),
                    record.getTime(),
                    record.getType(),
                    record.getId()));
          }
        } while (result.next());
      } else {
        LOGGER.fine("No records found from " + date);
      }
      result.close();
      statement.close();
      return list;
    } catch (final SQLException sqle) {
      LOGGER.severe(sqle.getMessage());
    }
    return null;
  }

  @Override
  public Day getDay(final User user, int day, int month, int year) {
    final List<Record> records = getDayRecords(user, day, month, year);
    final List<Session> sessions = Session.buildSessions(records);
    return Day.buildDay(sessions);
  }

  @Override
  public Month getMonth(User user, int year, int month) {
    return null;
  }

  @Override
  public List<Record> getMonthRecords(final User user, int month, int year) {
    final List<Record> list = new ArrayList<>();
    final LocalDate tmp = LocalDate.of(year, month, 1);
    Date startDate;
    Date endDate;
    try {
      startDate = Date.valueOf(tmp);
      endDate = Date.valueOf(LocalDate.of(year, month, tmp.lengthOfMonth()));
    } catch (final DateTimeException pe) {
      LOGGER.severe(pe.getMessage());
      return list;
    }

    final PreparedStatement statement;
    try {
      statement = connection.prepareStatement(SELECT_RECORDS_OF_MONTH);

      statement.setBytes(1, user.getUUIDBytes());
      statement.setDate(2, startDate);
      statement.setDate(3, endDate);

      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Searching for records between " + startDate + " and " + endDate);
      }

      final ResultSet result = statement.executeQuery();

      if (result.next()) {
        do {
          final Record record = new Record();
          record.setId(result.getInt("pk_record_id"));
          record.setDate(result.getDate("date").toLocalDate());
          record.setTime(result.getTime("time").toLocalTime());
          record.setType(RecordType.getType(result.getBoolean("type")));
          list.add(record);

          if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("Found record (%s %s, %s) with ID %s",
                    record.getDate(),
                    record.getTime(),
                    record.getType(),
                    record.getId()));
          }

        } while (result.next());
      } else {
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("No records found between " + startDate + " and " + endDate);
        }
      }

      result.close();
      statement.close();
      return list;
    } catch (final SQLException sqle) {
      LOGGER.severe(sqle.getMessage());
    }
    return null;
  }

  @Override
  public Year getYear(User user, int year) {
    return null;
  }

  @Override
  public List<Record> getYearRecords(User user, int year) {
    return null;
  }

  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}