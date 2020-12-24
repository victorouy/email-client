-- RUN ONCE

DROP DATABASE IF EXISTS JAGDB;
CREATE DATABASE JAGDB;

USE JAGDB;

DROP USER IF EXISTS victorouy@localhost;
CREATE USER victorouy@'localhost' IDENTIFIED BY 'dawson';
GRANT ALL ON JAGDB.* TO victorouy@'localhost';

FLUSH PRIVILEGES;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Attachments;
DROP TABLE IF EXISTS EmailToEmailAddress;
DROP TABLE IF EXISTS EmailAddresses;
DROP TABLE IF EXISTS Email;
DROP TABLE IF EXISTS Folders;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Folders (
    FolderKey int NOT NULL auto_increment,
    FolderName varchar(30) NOT NULL UNIQUE,
    PRIMARY KEY (FolderKey)
);

CREATE TABLE Email (
    EmailId int NOT NULL auto_increment,
    FromEmailAddress varchar(320) NOT NULL,
    Subject text,
    TextMessage text,
    HtmlMessage text,
    SentDate timestamp,
    ReceivedDate timestamp,
    FolderKey int NOT NULL,
    CONSTRAINT fk_folder_id FOREIGN KEY (FolderKey) REFERENCES Folders(FolderKey),
    PRIMARY KEY (EmailId)
);

CREATE TABLE EmailAddresses (
    EmailAddressId int NOT NULL auto_increment,
    EmailAddress varchar(320) NOT NULL UNIQUE,
    PRIMARY KEY (EmailAddressId)
);

CREATE TABLE EmailToEmailAddress (
    EmailToEmailAddressKey int NOT NULL auto_increment,
    EmailId int NOT NULL,
    EmailAddressId int NOT NULL,
    RecipientType varchar(3) NOT NULL,
    CONSTRAINT fk_email_emailaddress_id FOREIGN KEY (EmailId) REFERENCES Email(EmailId),
    CONSTRAINT fk_emailaddress_id FOREIGN KEY (EmailAddressId) REFERENCES EmailAddresses(EmailAddressId),
    PRIMARY KEY (EmailToEmailAddressKey)
);

CREATE TABLE Attachments (
    AttachmentId int NOT NULL auto_increment,
    EmailId int NOT NULL,
    FileName varchar(128) NOT NULL default '',
    CID varchar(50),
    BinaryData blob,
    CONSTRAINT fk_email_attachment_id FOREIGN KEY (EmailId) REFERENCES Email(EmailId),
    PRIMARY KEY (AttachmentId)
);

INSERT INTO Folders (FolderName)
VALUES ("INBOX");
INSERT INTO Folders (FolderName)
VALUES ("SENT");
INSERT INTO Folders (FolderName)
VALUES ("DRAFT");

INSERT INTO Email (FromEmailAddress, Subject, TextMessage, FolderKey, SentDate, ReceivedDate)
VALUES ("project.username01@gmail.com", "Test multiple recipients in one field", "Text message", 2, "2020-06-15 09:12:21", "2020-10-15 09:12:26");
INSERT INTO Email (FromEmailAddress, Subject, HtmlMessage, FolderKey, SentDate)
VALUES ("project.username01@gmail.com", "Test all recipient fields", "<p>html text in a p </p>", 2, "2020-11-15 08:04:11");
INSERT INTO Email (FromEmailAddress, Subject, FolderKey, SentDate)
VALUES ("project.username04@gmail.com", "No message", 2, "2020-10-15 10:54:34");
INSERT INTO Email (FromEmailAddress, TextMessage, FolderKey, SentDate)
VALUES ("project.username01@gmail.com", "No subject", 2, "2019-10-15 03:21:23");
INSERT INTO Email (FromEmailAddress, Subject, TextMessage, HtmlMessage, FolderKey, SentDate)
VALUES ("project.username02@gmail.com", "Inbox message", "the text message", "<p>the html message</p>", 1, "2020-10-01 09:34:21");
INSERT INTO Email (FromEmailAddress, Subject, HtmlMessage, FolderKey)
VALUES ("project.username01@gmail.com", "This is a drafted email to be updated", "<p>draft message</p>", 3);
INSERT INTO Email (FromEmailAddress, Subject, HtmlMessage, FolderKey)
VALUES ("project.username02@gmail.com", "Another Draft Email", "<p>draft message again</p>", 3);

INSERT INTO EmailAddresses (EmailAddress)
VALUES ("project.username01@gmail.com");
INSERT INTO EmailAddresses (EmailAddress)
VALUES ("project.username02@gmail.com");
INSERT INTO EmailAddresses (EmailAddress)
VALUES ("project.username03@gmail.com");
INSERT INTO EmailAddresses (EmailAddress)
VALUES ("project.username04@gmail.com");

INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (1, 1, "TO");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (1, 3, "TO");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (1, 4, "TO");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (2, 2, "TO");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (2, 1, "CC");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (2, 4, "BCC");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (3, 1, "TO");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (4, 1, "TO");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (4, 2, "TO");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (5, 1, "CC");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (6, 1, "TO");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (7, 3, "TO");
INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType)
VALUES (7, 2, "BCC");

-- INSERT INTO Attachments (EmailId, FileName, CID)
-- VALUES (1, "attachment2.jpg", "attachment2.jpg");
-- INSERT INTO Attachments (EmailId, FileName)
-- VALUES (2, "attachment1.jpg");
-- INSERT INTO Attachments (EmailId, FileName)
-- VALUES (6, "attachment1.jpg");