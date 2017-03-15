CREATE TABLE USERS (
  PK_USER_ID INT AUTO_INCREMENT(1, 1) PRIMARY KEY,
  UUID VARBINARY(16) UNIQUE NOT NULL ,
	USERNAME VARCHAR(32) UNIQUE NOT NULL,
	PASSWORD CHARACTER(32) NOT NULL, -- MD5
	FORENAME VARCHAR(64) NOT NULL,
	SURNAME VARCHAR(64) NOT NULL
)