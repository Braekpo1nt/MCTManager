-- Create event_info table
CREATE TABLE IF NOT EXISTS `event_info` (
        `eventId` VARCHAR(255) , 
        `plainTextName` VARCHAR(255) NOT NULL , 
        `componentName` VARCHAR(255) NOT NULL , 
        `createdDate` TIMESTAMP NOT NULL , 
        `modifiedDate` TIMESTAMP NOT NULL , 
        `eventDate` TIMESTAMP NOT NULL , 
        `startTime` TIMESTAMP , 
        `endTime` TIMESTAMP , 
        PRIMARY KEY (`eventId`) 
);


