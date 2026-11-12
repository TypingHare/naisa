### Deployment on GCP

This document explains, step by step, how to reproduce the server deployment
on Google Cloud Platform (GCP).

All group members have been given Owner access to the GCP project. To access,
go to `https://console.cloud.google.com`. In the top left, select columbia.edu
as the resource, and then in the **All** tab, select **Naisa** as the project. 
Our team mentor Ankit has also been given Viewer role access to the project.

> [!WARNING]
> Whenever not in use, PLEASE make sure to stop the VM to avoid wasting credits.

#### Setup from Scratch

##### GCP VM Configuration

In the GCP project, open the Navigation Menu in the top left corner and select
Compute Engine. Then, select Create instance. Choose a name and region for the
instance. Our server uses us-east4 (Northern Virginia) with Any zone.

For Machine type, we recommend a minimum requirement server of e2-medium, which 
has  the following specs: 2 shared vCPUs, 1 core, and 4GB of RAM.

For OS and storage, we are using Ubuntu 24.04 LTS Minimal x86/64 with a
Balanced persistent disk with a size of 10GB.

In Network settings, make sure External IPv4 Address is set to None.

All other settings are left default.

##### Configuring Internet Access

Use the below steps to reproduce Cloud NAT setup within the GCP console:

1. Go to **Network Services** -> **Cloud NAT**
2. Click "**Get Started**" or "**Create Cloud NAT gateway**"
3. Configure:
    - **gateway name:** naisa-nat
    - **NAT type:** Public
    - **Network:** default
    - **Region:** us-east4 (Northern Virginia)
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

These Cloud NAT settings allow the VM to make outbound connections only. 
It does not allow inbound connections.

##### Connecting to the Server

Once the above GCP setup steps are completed, run and connect to the server.
Select **Compute Engine** and **VM instances** from the GCP dashboard. Then, 
connect via **SSH** in the **Connect** column.

##### One-time Environment Setup Commands

For reference, these commands were run on the VM to setup the environment.

```bash
# Update apt
sudo apt-get update
sudo apt-get -y upgrade

# Install PostgreSQL 18
sudo apt install -y postgresql-common
sudo /usr/share/postgresql-common/pgdg/apt.postgresql.org.sh
sudo apt-get install -y postgresql-18

# Install Java 21
sudo apt-get install -y openjdk-21-jdk-headless

# Install git, make, and curl
sudo apt-get install -y git make curl

# Configuring the Naisa database on the VM:
sudo -u postgres createuser --pwprompt naisa
sudo -u postgres createdb --owner=naisa naisa

# Clone and configure the Naisa service
git clone --branch main https://github.com/TypingHare/naisa.git ~/naisa
cd ~/naisa
install -m 600 server/src/main/resources/database.example.properties \
  server/src/main/resources/database.properties
```

On the VM, open `server/src/main/resources/database.properties` in a text
editor. Set the datasource URL to `jdbc:postgresql://127.0.0.1:5432/naisa`
and the username to `naisa`. Add `spring.datasource.password` with the password
set when creating the `naisa` login. Then add the `server.address` line
below.

```properties
server.address=127.0.0.1
```

#### Starting the app in the server

Start the service while in the `~/naisa` directory using the following command:

```bash
make -C server run
```

In a separate SSH-in-browser, use `curl -i http://127.0.0.1:8080/v1/health` and
verify an HTTP 204 response to make sure the service is online.

### Stopping the app in the server

If on the SSH window where the server is being run, press CTRL+C to stop.
If you cannot find the window running the server, use the below to stop it:

```
# Find what is listening on the port:
sudo ss -ltnp 'sport = :8080'

# Use the PID from the above command for the following.
# Check that the process is the correct Naisa server
ps -fp PID

# Kill the process 
kill -TERM PID
```
Then, start the app again using the above instructions.

##### Update the app after changes to GitHub

To run the latest changes into `main` on the server, stop the app if it's
currently running, and then run the following commands from `~/naisa` in the
VM:

```bash
git pull --ff-only origin main
```

After updating, use the instructions in **Starting the app in the server** to
restart the app.