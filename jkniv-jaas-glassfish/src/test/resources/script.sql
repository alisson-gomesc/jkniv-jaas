    CREATE TABLE "AUTH_USER" 
    ( 
      "USERNAME" VARCHAR2(60) NOT NULL ENABLE, 
      "PASSWD" VARCHAR2(128) NOT NULL ENABLE, 
      CONSTRAINT "PK_USER" PRIMARY KEY ("USERNAME")
    )


    CREATE TABLE "AUTH_GROUP" 
    (    
      "USERNAME" VARCHAR2(60) NOT NULL ENABLE, 
      "GROUP_ID" VARCHAR2(30) NOT NULL ENABLE, 
      CONSTRAINT "PK_GROUP" PRIMARY KEY ("USERNAME", "GROUP_ID")
    )
    
    COMMIT;
    INSERT INTO AUTH_USER VALUES ('admin@localhost', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918');

    