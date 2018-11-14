# JAVA ftp implementation

2017030300 조성욱

## 2018 HYU  Computer Networks simplified ftp java implementation

## Custom FTP

- **Implemented commands**
  - LIST
  - CD
  - PUT
  - GET
  - QUIT

## Design

### Request

| header          |                  | body          |
| --------------- | ---------------- | ------------- |
| command (4byte) | arg size (4byte) | arg (or data) |

### Response

| header       |                     |                   | body |
| ------------ | ------------------- | ----------------- | ---- |
| Type (4byte) | Status Code (4byte) | Data size (4byte) | data |

## How to run

This project modulized for include client, server in one project.

If you are using jetbrain's Intellij IDE, just open src.zip and build artifacts. This job will create jar files.

Or, just use pre-built jar files.

If you want to build it on yourself, follow the 'How to build' instructions.
 
## How to build
1. Unzip source and cd to that directory.

#### Requirements
JDK > 1.8 (java8)
#### Server
```bash
$ cd FTPServer/src
$ javac FTPServer.java Server.java Handler.java
$ java FTPServer
```

#### Client
```bash
$ cd FTPClient/src
$ javac FTPClient.java Client.java
$ java FTPClient
```