## Goals
- [x] Create and edit users
- [x] Add processes and urls to track for each user
- [x] Create and Edit system events
- [x] Track time for each process
- [ ] Track time for each url
- [ ] Push system notifications
- [x] Edit processes
- [x] Stop process from running when reaching a time limit
- [x] Stop webpage from running
- [x] User interface
- [ ] Automatically create a user with the current system username if none exists
- [ ] Allow admin to connect remotely to the system
- [ ] Track total time spend on each process on the system
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
