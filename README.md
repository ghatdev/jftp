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

## How to build & run

This project modulized for include client, server in one project.

If you are using jetbrain's Intellij IDE, just open src.zip and build artifacts. This job will create jar files.

Or, just use pre-built jar files.