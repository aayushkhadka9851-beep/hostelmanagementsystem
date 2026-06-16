# Apache Tomcat 10 Deployment Guide

This guide describes how to build, configure, and deploy the **Hostel Management System** onto reference systems running **Apache Tomcat 10+**.

---

## 1. Prerequisites
Ensure the target server machine houses:
- **Java Development Kit (JDK) 17** (or above) installed and registered in your OS environment path (`JAVA_HOME`).
- **Apache Tomcat 10.x** installed.
- **MySQL Server 8.0+** running on standard port `3306`.

---

## 2. Compile & Pack via Maven
Open a terminal prompt at the root dir of `/java-project/` (the folder housing `pom.xml`) and run:
```bash
mvn clean package
```
### Output Artifacts
A successful compilation builds a fully self-contained web archive (`.war`) at:
```
/java-project/target/hostel-manager.war
```

---

## 3. Tomcat JNDI DataSource Configuration
Tomcat manages Database connection pooling safely. We must declare our authentication resource parameters.

1. Locate your Apache Tomcat installations directory.
2. Open the global layout config file `./conf/context.xml` in a text editor.
3. Paste the following `<Resource>` descriptor inside the root `<Context>` element tags:

```xml
<Context>
    <!-- Hostel Management System MySQL Pool -->
    <Resource name="jdbc/HostelDB"
              auth="Container"
              type="javax.sql.DataSource"
              maxTotal="50"
              maxIdle="15"
              maxWaitMillis="10000"
              username="root"
              password="admin123"
              driverClassName="com.mysql.cj.jdbc.Driver"
              url="jdbc:mysql://localhost:3306/hostel_db?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;serverTimezone=UTC&amp;charEncoding=UTF-8" />
</Context>
```
*Note: Replace `username` and `password` with your active local MySQL parameters.*

---

## 4. Deploying the WAR file

### Method A: Direct File copy (Recommended)
1. Stop your Tomcat server (run `./bin/shutdown.sh` or shutdown script).
2. Copy the compiled `./target/hostel-manager.war` from Maven.
3. Paste the file directly into Tomcat's deployment folder:
   ```
   [TOMCAT_INSTALL_DIR]/webapps/
   ```
4. Start your Tomcat server (run `./bin/startup.sh`).
5. Tomcat automatically unzips the WAR file, registers the annotations, and launches the servlet container.

### Method B: Tomcat Web Manager App
1. Direct your browser to the local manager dashboard: `http://localhost:8080/manager/html`.
2. Scroll to the section labeled **"WAR file to deploy"**.
3. Click "Choose File" and select `/java-project/target/hostel-manager.war`.
4. Click **Deploy**.

---

## 5. Launch and Verify
Once deployed, click or navigate to:
```
http://localhost:8080/hostel-manager/
```
You will be greeted by the elegant Royal Oak Login screen! Use credential **admin** / **admin123** to start supervising students and room assets.
