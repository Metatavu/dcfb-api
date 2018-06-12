# DCFB API

API Service for DCFBs.

## Installing 

### Prerequisites

These instructions assume that system is being installed on machine with Ubuntu 16.04 OS. 

### Set environment

Choose installation directory

    export INSTALL_DIR=[Where you want to install the application] 
   
Add desired hostname into hosts file and change it to point to 127.0.0.1. In this example we use dev.dcfb.fi

### Install Postgres

    sudo apt-get update
    sudo apt-get install postgresql postgresql-contrib

### Create database and database user

    sudo -u postgres createuser -R -S dcfbapi
    sudo -u postgres createdb -Odcfbapi -Ttemplate0 dcfbapi
    sudo -u postgres psql 
    alter user dcfbapi with password 'password';    
    
### Install Java

*Note that application does not compile with Java 10*
  
    sudo apt-get install openjdk-8-jdk openjdk-8-jre

### Install Wildfly

    cd $INSTALL_DIR
    wget "http://download.jboss.org/wildfly/13.0.0.Final/wildfly-13.0.0.Final.zip"
    unzip wildfly-13.0.0.Final.zip
    
### Install Wildfly Postgres Module

    cd $INSTALL_DIR/wildfly-13.0.0.Final
    wget https://www.dropbox.com/s/lt5r6r3grz8gl9s/postgresql-wildfly-module.zip?dl=1 -O postgresql-wildfly-module.zip
    unzip postgresql-wildfly-module.zip
    
### Install Wildfly Keycloak Adapter

    cd $INSTALL_DIR/wildfly-13.0.0.Final
    wget https://downloads.jboss.org/keycloak/4.0.0.Beta3/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-4.0.0.Beta3.zip
    unzip keycloak-wildfly-adapter-dist-4.0.0.Beta3.zip
    sh bin/jboss-cli.sh --file=bin/adapter-elytron-install-offline.cli
    rm keycloak-wildfly-adapter-dist-4.0.0.Beta3.zip
    
### Install Keycloak

     cd $INSTALL_DIR
     wget https://downloads.jboss.org/keycloak/3.4.3.Final/keycloak-3.4.3.Final.zip
     unzip keycloak-3.4.3.Final.zip
     
### Start Keycloak

In order to use the API, Keycloak must be running, so starting it in another console would be a good idea.

     cd $INSTALL_DIR/keycloak-3.4.3.Final/bin/
     sh standalone.sh -Djboss.socket.binding.port-offset=200
     
### Configure Wildfly

Start Wildfly in background by running following script:

*Note that you need to replace [INSTALL_FOLDER] with your installation folder and [PASSWORD] with your postgres user password.
    
    cd $INSTALL_DIR/wildfly-13.0.0.Final/bin
    sh jboss-cli.sh
    embed-server --server-config=standalone.xml
    /subsystem=datasources/jdbc-driver=postgres:add(driver-module-name="org.postgres",driver-xa-datasource-class-name="org.postgresql.xa.PGXADataSource",driver-datasource-class-name="org.postgresql.ds.PGSimpleDataSource")
    /subsystem=datasources/xa-data-source=dcfb:add(jndi-name="java:jboss/datasources/dcfb-api", user-name="username", password="[PASSWORD]", driver-name="postgres")
    /subsystem=datasources/xa-data-source=dcfb/xa-datasource-properties=ServerName:add(value="localhost")
    /subsystem=datasources/xa-data-source=dcfb/xa-datasource-properties=DatabaseName:add(value="dcfbapi")
    /subsystem=undertow/server=default-server/host=dcfb-api:add(default-web-module="dcfb-api.war",alias=["dev.dcfb.fi"])
    exit
     
### Setup Keycloak realm

If you have an export file, you can configure your Keycloak from it by starting the Keycloak one time with following command:

     sh $INSTALL_DIR/keycloak-3.4.3.Final/bin/standalone.sh -Djboss.socket.binding.port-offset=200 -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=yourmigrationfile.json
     
If you don't have an export file, you need to do following steps to create new realm and client: 

- Navigate into *http://localhost:8280/auth*. 
- Create admin user
- Login with newly created user
- Create new realm
- Create new client with following settings:
  - Client Protocol: openid-connect
  - Access Type: bearer-only
- Click installation tab
- Select Keycloak OIDC JBoss Subsystem XML
- Copy the XML
- Change secure-deployment name to "dcfb-api.war" in the XML
- Edit $INSTALL_DIR/wildfly-13.0.0.Final/standalone/configuration/standalone.xml and add the configuration under 
- Paste the XML under <subsystem xmlns="urn:jboss:domain:keycloak:1.1"> -section
  
### Compile and deploy DCFB API

Compile application

    sudo apt install git maven
    cd $INSTALL_DIR
    git clone https://github.com/Metatavu/dcfb-api.git
    mvn clean package
    
Deploy by copying war-archive into the Wildfly deployments -folder:

    cp $INSTALL_DIR/dcfb-api/target/*.war $INSTALL_DIR/wildfly-13.0.0.Final/standalone/deployments/
    
And start the Wildfly by running

    $INSTALL_DIR/wildfly-13.0.0.Final/bin/standalone.sh
