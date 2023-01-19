# JoshBookClient
(For the Server Side of the Messaging App please go here -> https://github.com/JoshoeaLee/JoshBookServer )

A live messaging app which has a server side and client sides with messages being stored in an intermediatory Azure SQL database.
Individual session keys(AES) are created for clients when they log-in. 
These are encrypted using asymmetrical encryption methods (RSA) using the Server Public key to encrypt the AES session keys and then when the server receives the message, it decrypts using its private key.

(Originally deployed on an Azure VM but then taken off to save money)

Technologies Used: Java, Java-FX, Azure SQL, RSA/AES Encryption, Azure VM

<img width="568" alt="message-app-pic" src="https://user-images.githubusercontent.com/114985386/213414686-c283e933-c449-4640-aabf-24df31f608bf.png">
