USE JAGDB;

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