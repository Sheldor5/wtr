package at.sheldor5.tr.api.utils;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

public class UuidUtils {

  public static byte[] getBytes(final UUID uuid) {
    if (uuid == null) {
      return null;
    }
    final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

  public static UUID getUuid(byte[] bytes) {
    if (bytes == null || bytes.length != 16) {
      return null;
    }
    final ByteBuffer bb = ByteBuffer.wrap(bytes);
    long mostSigBits = bb.getLong();
    long leastSigBits = bb.getLong();
    return new UUID(mostSigBits, leastSigBits);
  }

  public static Byte[] toObject(byte[] bytes) {
    Byte[] result = new Byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      result[i] = bytes[i];
    }
    return result;
  }

  public static byte[] toPrimitive(Byte[] bytes) {
    byte[] result = new byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      result[i] = bytes[i];
    }
    return result;
  }

  public static byte[] getRandomUuidBytes() {
    return getBytes(UUID.randomUUID());
  }

  /**
   * Converts a byte array into an {@link UUID} object.
   * <p/>
   * Microsoft stores GUIDs in a binary format that differs from the RFC standard of UUIDs (RFC #4122). (See details
   * at http://en.wikipedia.org/wiki/Globally_unique_identifier) This function takes a byte array read from Active
   * Directory and correctly decodes it as a {@link UUID} object.
   *
   * @param bytes Byte array received as en entry attribute from Active Directory.
   * @return {@link UUID} object created from the byte array, or null in case the passed array is not exactly 16 bytes long.
   */
  public static UUID objectGUIDbytesToUUID(byte[] bytes) {
    if (bytes != null && bytes.length == 16) {
      long msb = bytes[3] & 0xFF;
      msb = msb << 8 | (bytes[2] & 0xFF);
      msb = msb << 8 | (bytes[1] & 0xFF);
      msb = msb << 8 | (bytes[0] & 0xFF);

      msb = msb << 8 | (bytes[5] & 0xFF);
      msb = msb << 8 | (bytes[4] & 0xFF);

      msb = msb << 8 | (bytes[7] & 0xFF);
      msb = msb << 8 | (bytes[6] & 0xFF);

      long lsb = bytes[8] & 0xFF;
      lsb = lsb << 8 | (bytes[9] & 0xFF);
      lsb = lsb << 8 | (bytes[10] & 0xFF);
      lsb = lsb << 8 | (bytes[11] & 0xFF);
      lsb = lsb << 8 | (bytes[12] & 0xFF);
      lsb = lsb << 8 | (bytes[13] & 0xFF);
      lsb = lsb << 8 | (bytes[14] & 0xFF);
      lsb = lsb << 8 | (bytes[15] & 0xFF);

      return new UUID(msb, lsb);
    }
    return null;
  }
}