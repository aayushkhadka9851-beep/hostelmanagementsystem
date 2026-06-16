# IntelliJ IDEA Setup Guide

This tutorial describes step-by-step directions to import, configure, run, and debug the **Hostel Management System** project inside **IntelliJ IDEA** (Ultimate Edition is highly recommended for direct Jakarta EE support).

---

## Step 1: Open the Project
1. Launch **IntelliJ IDEA**.
2. Select **Open** or **Import**.
3. Choose the directory `/java-project/` (the folder housing `pom.xml`).
4. Click **OK**. Select **Trust Project** if prompted.

---

## Step 2: Configure Maven & JDK Environment
1. Wait for IntelliJ to index and pull down dependencies.
2. Go to **File** > **Project Structure** (or press `Ctrl+Alt+Shift+S` / `Cmd+;`).
3. Under the **Project** tab:
   - Ensure the SDK matches **JDK 17**.
   - Check that compile output levels match **Language Level 17**.
4. Click **Apply**.

---

## Step 3: Link Apache Tomcat 10 Server
To run and hot-reload Servlets inside IntelliJ, link your physical Tomcat installation:

1. Click on **Current File** or the configuration dropdown at the top right, and select **Edit Configurations...**.
2. Click the green **`+` (Add New Configuration)** icon.
3. Select **Tomcat Server** > **Local** (ensure you do not select "Tomcat Server (Docker)" or "Tomote").
4. Click **Configure...** near the "Application Server" dropdown.
5. Select your physical directory where **Apache Tomcat 10.x** is located. Click **OK**.

---

## Step 4: Configure Artifact Deployments
1. Inside the same Local Tomcat Configuration pane, look for a red warning at the bottom saying: *"Warning: No artifacts marked for deployment"*.
2. Click the **Fix** button.
3. Select:
   ```
   hostel-management-system:war exploded
   ```
   *(We select the "war exploded" artifact to allow instant JVM hot-swapping during code changes).*
4. In the **Application context** text box (at the bottom of the Deployment tab), set the path to:
   ```
   /hostel-manager
   ```
5. Click **OK** to save configurations.

---

## Step 5: Test Execution & Debugging

### Direct Execution
- Press the green **Run** triangle button at the top-right toolbar.
- IntelliJ compiles with Maven, launches the local Tomcat 10 cluster, links database threads, and launches your default browser at `http://localhost:8080/hostel-manager/`.

### Interactive Debugging (Critical for Classrooms)
- Press the green **Bug** icon instead of the triangle Run button.
- You can place breakpoints (by clicking in the left margin next to lines of code) in file controllers like `StudentServlet.java` or `DBConnection.java`.
- When you execute student registrations in your browser, IntelliJ freezes thread execution at your breakpoints. This allows you to inspect variable parameters, step over SQL updates, and analyze memory logs in real-time.
