### Deployment on GCP

This document explains, step by step, how to reproduce the server deployment
on Google Cloud Platform (GCP).

All group members have been given Owner access to the GCP project. To access,
go to `https://console.cloud.google.com`. In the top left, select columbia.edu
as the resource, and then in the **All** tab, select **Naisa** as the project. Our team
mentor Ankit has also been given Viewer role access to the project.

> [!WARNING]
> Whenever not in use, PLEASE make sure to stop the VM to avoid wasting credits.

#### Setup

To run and connect to the server, select **Compute Engine** and **VM instances**
from the GCP dashboard. Then, connect via **SSH** in the **Connect** column.

##### Building and Uploading new JAR files

Build the project locally and then upload the executable JAR to the VM. To
build the JAR, follow these steps from the Naisa repo root:

**On Mac:**

```bash
build_dir=$(mktemp -d)
git archive HEAD server | tar -x -C "$build_dir"
cd "$build_dir/server"
./mvnw -DskipTests package

sed '/^spring.config.import=/d' src/main/resources/application.properties \
  > target/naisa-application.properties

open target
```

The above command builds the current commit in a temp directory. This prepares
a server config file from the project defaults and opens the folder.

The build names the JAR `demo-<version>.jar`. In Finder, rename the file that
ends in `.jar` to `naisa.jar` before you upload it.

In the SSH-in-browser, press the **Upload File** button in the top right and
upload `naisa.jar`. This puts the JAR into the home directory of the VM. Use
`ls` within the VM terminal to verify successful upload.

##### Start the app in the server

One the JAR file is uploaded onto the VM, use the following command to start
the server:

```bash
java -jar ~/naisa.jar \
  --spring.config.location="file:$HOME/naisa-application.properties"
```

In a separate SSH-in-browser, use `curl -i http://127.0.0.1:8080/v1/health` and
verify an HTTP 204 response to make sure the service is online.

##### Update the app after changes to GitHub

After new changes are merged into `main`, run these commands from the Naisa
repo root on Mac:

```bash
git switch main
git pull --ff-only origin main
```

Follow the Mac build steps above through `./mvnw -DskipTests package`. After
Maven reports `BUILD SUCCESS`, run `open target`. For a routine update, keep
the existing `naisa-application.properties` on the VM.

If the app is running, press **Ctrl+C** in the SSH window where it is running
to stop it. Use the upload steps above to replace `naisa.jar`. Then use
the same start command and check that `/v1/health` returns HTTP 204.

#### VM Configuration

The server uses the followinig machine configuration: e2-medium with 2 vCPU,
1 core, and 4GB of memory.

For OS and storage, we are using Ubuntu 24.04 LTS Minimal x86/64 with a
Balanced persistent disk.

#### Configuring Internet Access

Use the below steps to reproduce Cloud NAT setup within the GCP console:

1. Go to **Network Services** -> **Cloud NAT**
2. Click "**Get Started**" or "**Create Cloud NAT gateway**"
3. Configure:
    - **gateway name:** naisa-nat
    - **NAT type:** Public
    - **Network:** default
    - **Region:** us-east-4 (Northern Virginia)
4. Under "**Cloud Router**":
    - **name:** naisa-router
    - everything else left default
5. Cloud NAT mapping:
    - **source endpoint type:** VM instances, GKE nodes, Serverless
    - **Source IP version:** IPv4 subnet ranges
    - **IPv4 subnet ranges:** Custom, and then:
    - **Subnets:** default
    - **IP ranges:** Primary IP range
6. Finally, select:
    - **Cloud NAT IP addresses:** Automatic
    - **Network Service Tier:** Standard
    - Leave all other settings at default and create

#### One-time Environment Setup Commands

For reference, these commands were run on the VM to setup the environment.

**Update apt:**

```bash
sudo apt-get update
sudo apt-get -y upgrade
```

**PostgreSQL:**

```bash
sudo apt install -y postgresql-common
sudo /usr/share/postgresql-common/pgdg/apt.postgresql.org.sh
sudo apt-get install -y postgresql-18
```

**Java:**

```bash
sudo apt install -y openjdk-21-jre-headless
```

**Neovim:**

```bash
sudo apt install neovim
```

**Configuring Naisa on the VM:**

```bash
sudo -u postgres createuser --pwprompt naisa
sudo -u postgres createdb --owner=naisa naisa
```

To protect the config file and then open it for editing:

```bash
chmod 600 ~/naisa-application.properties
nvim ~/naisa-application.properties
```

Then, the below lines were added into the file, replacing
"YOUR_DATABASE_PASSWORD" with the password set for the db login.

```properties
spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/naisa
spring.datasource.username=naisa
spring.datasource.password=YOUR_DATABASE_PASSWORD
server.address=127.0.0.1
```
