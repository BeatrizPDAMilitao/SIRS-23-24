# SETUP TUTORIAL FOR THE POSTGRES DATABASE

## INSTALL DEPENDENCIES

-   For the development of the project we decided to use Postgresql-16, which
    is already installed in kali linux, so there are no required dependencies to
    install.

## STEPS TO REPRODUCE

1. Enable Remote Access,

   -   For that we need to edit the /etc/postgresql/16/main/postgresql.conf

    ```console
   sudo nano /etc/postgresql/16/main/postgresql.conf
    ```

   -   Under ‘Connection and Authentication’, uncomment line 59;
       #listen_addresses=’localhost’ and change it to

    ```console
   listen_addresses='*'
    ```

2. Map the system user name to 'postgres'

   -   For that we need to edit the /etc/postgresql/16/main/pg_ident.conf

    ```console
    sudo nano /etc/postgresql/16/main/pg_ident.conf
    ```

   -   Add the following lines to the end of the file

    ```text
    user1   kali    postgres
    user2   kali    kali
    ```

3. Change the client authentication file

   -   For that we need to edit the /etc/postgresql/16/main/pg_hba.conf

    ```console
   sudo nano /etc/postgresql/16/main/pg_hba.conf
    ```

   -   Add the following text to the end of line 90 (local all postgres peer)

    ```text
    map=user1
    ```

   -   You will end up with something like this

    ```text
    local all postgres               peer map=user1 
    ```

   -  Now create a new authentication in order to set up our database user for
      that add the following line bellow the one we have just altered

    ```text
    local bombapetit kali             peer map=user2 
    ```

   -   Allow external connections via tcp. To do that change the IP address/subnet mask
       from 127.0.0.1/32 to 0.0.0.0/0

4. SSL setup

   -   Copy the database keys (in the database folder of the project) to the Postgresql data directory
    ```console
   sudo cp Keys/database.* /var/lib/postgresql/16/main/
    ```

   -   Set the owner of the keys to postgres
    ```console
   sudo chown postgres:postgres /var/lib/postgresql/16/main/database.key
   sudo chmod 600 /var/lib/postgresql/16/main/database.key
    ```

   -   Set postgres to use ssl connections for that go to the ssl section in /etc/postgresql/16/main/postgresql.conf, and
       uncomment/change the following lines
    ```console
    ssl = on
    ssl_cert_file = 'database.crt'
    ssl_key_file = 'database.key'
    ```
5. Start the postgresql service

    ```console
    sudo systemctl start postgresql.service
    ```

    ```console
    sudo systemctl enable postgresql.service
    ```

6. Run the automation script

    ```console
    sh postgres.sh
    ```

   - NOTE: in the case the file is not executable run the following command:
      ```console
      chmod +x postgres.sh
      ```

