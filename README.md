## Goals
- [x] Create and edit users
- [x] Add processes and urls to track for each user
- [x] Create and Edit system events
- [x] Track time for each process
- [ ] Track time for each url (not priority for now)
- [ ] Push system notifications
- [x] Edit processes
- [x] Stop process from running when reaching a time limit
- [x] Stop webpage from running
- [x] User interface
- [ ] Allow admin to connect remotely to the system
- [x] Lock system after x hour
- [ ] Lock system after n hours screen time
- [x] Show app in system tray
- [x] Run at startup without requiring admin password
- [x] Admin password required to change settings or to close the app

## Setup

> Make sure you have Java 21+ and Maven installed and properly configured in your environment.
>
> Make sure you have [Maven](https://maven.apache.org/install.html) installed and properly configured on your system.

1. Clone de repository
```commandline
    git clone https://github.com/zKlau/ParentalControl.git
    cd ParentalControl
```
2. Build the Project
```commandline
    mvn clean install
```

3. Run the application
```commandline
    mvn javafx:run
```
