# concertify

Workflow -flowchart - [Mermaid studio:](https://www.mermaidchart.com/play?utm_source=mermaid_js&utm_medium=editor_selection&utm_campaign=playground)
---
## Auth Service
```mermaid
flowchart TD
    Start[Auth Service]

    %% Create Group
    Start --> CG[POST /auth/groups]
    CG --> CG_Auth[Check Role: global_admin]
    CG_Auth --> CG_Validate[Validate Input]
    CG_Validate --> CG_CreateParent[Ensure /global region group]
    CG_CreateParent --> CG_CreateChild[Ensure /global region type group]
    CG_CreateChild --> CG_Role[Ensure type_region role]
    CG_Role --> CG_Assign[Assign role to group]
    CG_Assign --> CG_Done[Return Group ID]

    %% Create User (Admin Use Case)
    Start --> CU_Admin[POST /auth/users]
    CU_Admin --> CU_Admin_Auth[Check Role: administrator]
    CU_Admin_Auth --> CU_Admin_Validate[Validate Input]
    CU_Admin_Validate --> CU_Admin_Group[Ensure Groups and Roles]
    CU_Admin_Group --> CU_Admin_CreateUser[Create User]
    CU_Admin_CreateUser --> CU_Admin_AssignGroup[Assign User to Group]
    CU_Admin_AssignGroup --> CU_Admin_Done[Return User ID]

    %% Create User (Customer Sign-Up)
    Start --> CU_Customer[POST /auth/sign-up]
    CU_Customer --> CU_Customer_Role[Check Role: customer]
    CU_Customer_Role --> CU_Customer_Validate[Validate User Info]
    CU_Customer_Validate --> CU_Customer_Region[Determine Region]
    CU_Customer_Region --> CU_Customer_CreateUser[Create User Enabled]
    CU_Customer_CreateUser --> CU_Customer_Done[Return User ID]

    %% Update User
    Start --> UU[PUT /auth/users]
    UU --> UU_Variants{Use Case}
    UU_Variants --> UU_Admin[Administrator Actions]
    UU_Variants --> UU_Customer[Customer Own Info]

    %% Delete User
    Start --> DU[DELETE /auth/users]
    DU --> DU_Auth[Check Role: administrator]
    DU_Auth --> DU_Delete[Delete User]

    %% User Details
    Start --> UD_Admin[GET /auth/users/id]
    UD_Admin --> UD_Admin_Auth[Check Role: administrator]

    Start --> UD_Customer[GET /auth/info]
    UD_Customer --> UD_Customer_Check[Check Owner Identity]
```
---
## Concert Service
```mermaid
flowchart TD
  Start([Concert Service]) 

  %% Create
  Start --> CreateConcert[POST_concerts]
  CreateConcert --> CC_Role[Check Role: organizer]
  CC_Role --> CC_Validate[Validate Inputs]
  CC_Validate --> CC_Region[Determine Region]
  CC_Region --> CC_Create[Create Concert]
  CC_Create --> CC_Seats[Generate Seats Map]
  CC_Seats --> CC_Success[Return New Concert ID]
  CC_Seats -->|Fail| CC_Rollback[Rollback Delete]

  %% Update
  Start --> UpdateConcert[PUT_concerts_id]
  UpdateConcert --> UC_Role[Check Role: organizer & isOwner]
  UC_Role --> UC_Validate[Validate Inputs]
  UC_Validate --> UC_CheckExist[Check if Exists]
  UC_CheckExist --> UC_Update[Update Concert Fields]
  UC_Update --> UC_Success[Return Updated Concert ID]

  %% Delete
  Start --> DeleteConcert[DELETE_concerts_id]
  DeleteConcert --> DC_Role[Check Role: organizer & isOwner]
  DC_Role --> DC_CheckExist[Check if Exists]
  DC_CheckExist --> DC_SoftDelete[Set isDeleted=true]
  DC_SoftDelete --> DC_Success[Return Updated Concert ID]

  %% Details
  Start --> DetailOrganizer[GET_concerts_id_owner]
  DetailOrganizer --> DO_Role[Check Role: organizer & isOwner]
  DO_Role --> DO_CheckExist[Check if Exists]
  DO_CheckExist --> DO_Return[Return Full Details]

  Start --> DetailCustomer[GET_concerts_id]
  DetailCustomer --> DC_Role[Check Role: customer]
  DC_Role --> DC_Region[Check Region Match]
  DC_Region --> DC_CheckExist[Check if Exists]
  DC_CheckExist --> DC_Return[Return Details - No audit info]

  %% Search
  Start --> SearchOrganizer[POST_concerts_me]
  SearchOrganizer --> SO_Role[Check Role: organizer]
  SO_Role --> SO_Query[Query Owner & Region All isDeleted]
  SO_Query --> SO_Return[Return Page of Concerts]

  Start --> SearchCustomer[POST_concerts]
  SearchCustomer --> SC_Role[Check Role: customer]
  SC_Role --> SC_Query[Query Region isDeleted=false]
  SC_Query --> SC_Return[Return Page of Concerts]

```
---
## Booking Service
```mermaid
flowchart TD
  Start([Booking Service])

  %% 1. Create Booking
  Start --> CreateBooking[POST_bookings]
  CreateBooking --> CB_Role[Check Role: customer]
  CB_Role --> CB_Validate[Validate Inputs]
  CB_Validate --> CB_Region[Determine Region]
  CB_Region --> CB_User[Get User ID from JWT]
  CB_User --> CB_Create[Create Booking]
  CB_Create --> CB_Items[Create Booking Items]
  CB_Items --> CB_Kafka[Send Kafka to Concert Service]
  CB_Kafka --> CB_Success[Return New Booking ID]
  CB_Kafka -->|Fail| CB_Rollback[Rollback: Delete Booking + Notify User]

  %% 2. Update Booking
  Start --> UpdateBooking[PUT_bookings]
  UpdateBooking --> UB_Role[Check Role: customer & isOwner]
  UB_Role --> UB_Validate[Validate Inputs]
  UB_Validate --> UB_CheckExist[Check if Exists]
  UB_CheckExist --> UB_NewItems[Create New Items]
  UB_NewItems --> UB_DelItems[Delete Items]
  UB_DelItems --> UB_Kafka[Send Kafka to Concert Service]
  UB_Kafka --> UB_Success[Return Updated Booking ID]
  UB_Kafka -->|Fail| UB_Rollback[Rollback: Delete New Items, Restore Deleted Items + Notify User]

  %% 3. Update Booking Status
  Start --> UpdateStatus[PATCH_bookings]
  UpdateStatus --> US_Role[Check Role: organizer & isOwnerOfConcert]
  US_Role --> US_Validate[Validate Inputs]
  US_Validate --> US_CheckExist[Check if Exists]
  US_CheckExist --> US_Update[Update Status]
  US_Update --> US_Notify[Notify User]
  US_Notify --> US_Success[Return Updated Booking ID]

  %% 4. Delete Booking
  Start --> DeleteBooking[DELETE_bookings]
  DeleteBooking --> DB_Role[Check Role: customer & isOwner]
  DB_Role --> DB_CheckExist[Check if Exists]
  DB_CheckExist --> DB_Delete[Delete Booking + Items]
  DB_Delete --> DB_Kafka[Send Kafka to Concert Service]
  DB_Kafka --> DB_Success[Return Deleted Booking ID]
  DB_Kafka -->|Fail| DB_Rollback[Rollback: Restore Booking + Notify User]

  %% 5. Booking Details
  Start --> DetailsOrganizer[GET_bookings_id]
  DetailsOrganizer --> DO_Role[Check Role: organizer & isOwnerOfConcert]
  DO_Role --> DO_CheckExist[Check if Exists]
  DO_CheckExist --> DO_Return[Return Booking Details]

  Start --> DetailsCustomer[GET_bookings_id_owner]
  DetailsCustomer --> DC_Role[Check Role: customer & isOwner]
  DC_Role --> DC_Region[Check Region Match]
  DC_Region --> DC_CheckExist[Check if Exists]
  DC_CheckExist --> DC_Return[Return Booking Details]

  %% 6. Search Bookings
  Start --> SearchOrganizer[GET_bookings_concertId]
  SearchOrganizer --> SO_Role[Check Role: organizer]
  SO_Role --> SO_Query[Query by Concert Owner & Region - All isDeleted]
  SO_Query --> SO_Return[Return Page of Bookings]

  Start --> SearchCustomer[GET_bookings_me]
  SearchCustomer --> SC_Role[Check Role: customer]
  SC_Role --> SC_Query[Query by Owner & Region - isDeleted=false]
  SC_Query --> SC_Return[Return Page of Bookings]
```
---
# RBAC Graph for Keycloak Realm
```
Realm: `concertify`
|
├── Groups (/global/{region}/{type_of_user})
|   |
│   ├── /global/{region}/organizers
│   │   └── Realm Role: organizer_{region}
|   |
│   ├── /global/{region}/customers
│   │   └── Realm Role: customer_{region}
|   |
│   └── /global/{region}/administrators
│       └── Realm Role: administrator_{region}
|
├── Clients
|   |
│   ├── auht-service
│   │   ├── Resource: users, roles, groups
│   │   ├── Scopes:       
|   |   |   ├── view,update               → customer_{region} & organizer_{region} (check owner in backend)
│   │   │   └── read,create,update,delete → administrator_{region}
│   │   └── Policy: Access realm
│   │       └── Role-based: administrator
|   |
│   ├── concert-service
│   │   ├── Resource: {region} concerts
│   │   ├── Scopes:
│   │   │   ├── view                      → customer_{region} (check owner in backend)
│   │   │   └── read,create,update,delete → organizer_{region} (check owner in backend)
│   │   └── Policy: Access {region} concerts
│   │       └── Role-based (customer/organizer)
│   |
│   └── booking-service
│       ├── Resource: {region} bookings
│       ├── Scopes:
│       │   ├── create,read,update        → customer_{region} (check owner in backend)
│       │   └── read,update               → organizer_{region} (check concert' s owner in backend)
│       └── Policy: Access {region} bookings
│           └── Role-based (customer/organizer)
|
└── Realm Roles
    ├── customer_{region}
    ├── organizer_{region}
    └── administrator_{region}
```

# Features Details

## Auth Service

### 1. Create Group: [POST] /auth/groups

- **Required Role:** `global_admin`
- **Input:**
    - `type_of_user`: `administrator` | `customer` | `organizer`
    - `region`: e.g. `en`, `vi`, `fr`, ...
- **Process:**
    1. Create `/global/{region}` group if it does not exist.
    2. Create `/global/{region}/{type_of_user}` group if it does not exist.
    3. Create `{type_of_user}_{region}` role if it does not exist.
    4. Assign `{type_of_user}_{region}` role to `/global/{region}/{type_of_user}` group.
- **Output:**
    - ID of `/global/{region}/{type_of_user}` group.

---

### 2. Create User (Administrator Use Case): [POST] /auth/users

- **Required Role:** `administrator`
- **Input:**
    - `type_of_user`: `administrator` | `organizer`
    - `region`: e.g. `en`, `vi`, `fr`, ...
- **Process:**
    1. Validate inputs.
    2. Create `/global/{region}` group if it does not exist.
    3. Create `/global/{region}/{type_of_user}` group if it does not exist.
    4. Create `{type_of_user}_{region}` role if it does not exist.
    5. Assign `{type_of_user}_{region}` role to `/global/{region}/{type_of_user}` group.
- **Output:**
    - ID of `/global/{region}/{type_of_user}` group.

---

### 3. Create User (Customer Sign-Up): [POST] /auth/sign-up

- **Required Role:** `customer`
- **Input:**
    - `user_info`:
        - `email` (required)
        - `password` (required)
        - `firstName`
        - `lastName` (required)
- **Process:**
    1. Validate inputs.
    2. Determine current region (e.g. from `Accept-Language` header or other source).
    3. Create new user with:
    - `enabled = true`
    - `emailVerified = false`
- **Output:**
    - ID of new user.

---

### 4. Update User: [PUT] /auth/users

- **Variants:**
    - **Administrator Use Case**:
        - Can enable/disable user
        - Can mark email as verified/unverified
    - **Customer (Owner) Use Case**:
        - Can update their own information

---

### 5. Delete User: [DELETE] /auth/users

- **Required Role:** `administrator`

---

### 6. User Details

- **Allowed Roles:**
    - `administrator` - [GET] /auth/users/{id}
    - Customer (only if requesting their own user details) - [GET] /auth/info

---
## Concert Service

### 1. Create Concert: [POST] /concerts

- **Required Role:** `organizer`
- **Input:**
    - `concert_info`:
        - `titile` (required)
        - `description` 
        - `location` (required)
        - `startTime` (required)
        - `endTime` (required)
- **Process:**
    1. Validate inputs.
    2. Determine current region (e.g. from `Accept-Language` header or other source).
    3. Create new concert.
    4. Generate seats map for new concert.
        - Rollback if failed: delete new concert
- **Output:**
    - ID of new concert.

---

### 2. Update Concert: [PUT] /concerts/{id}

- **Required Role:** `organizer` and isOwner=true
- **Input:**
    - `concert_info`:
        - `id` (required)
        - `titile` (required)
        - `description`
        - `location` (required)
        - `startTime` (required)
        - `endTime` (required)
- **Process:**
    1. Validate inputs.
    2. Check if existed.
    3. Update concert on only input fields.
- **Output:**
    - ID of updated concert.

---

### 3. Soft delete Concert: [DELETE] /concerts/{id}

- **Required Role:** `organizer` and isOwner=true
- **Input:**
    - `concert_id` (required)
- **Process:**
    1. Check if existed.
    2. Update isDeleted=true.
- **Output:**
    - ID of updated concert.

---

### 4. Concert Details
- **Variants:**
    - **Organizer Use Case**: [GET] /concerts/{id}/owner
      - **Required Role:** `organizer` and isOwner=true
      - **Input:**
        - `concert_id` (required)
      - **Process:**
          1. Check if existed.
          2. Query by ID.
      - **Output:**
          - Details of concert (full info).

    - **Customer Use Case**:  [GET] /concerts/{id}
      - **Required Role:** `customer`
      - **Input:**
        - `concert_id` (required)
      - **Process:**
          1. Check if region is matched.
          2. Check if existed.
          3. Query by ID.
      - **Output:**
          - Details of concert (except audit info).

---

### 5. Search Concerts

- **Variants:**
    - **Organizer Use Case**: [POST] /concerts/me?offset=&limit=
        - **Input:**
            - `query` 
            - `offset` (begin at 0)
            - `limit` (default 20)
        - **Process:**
            1. Query by owner ID & region include isDeleted=true.
        - **Output:**
            - Page of concerts (preview info).

    - **Customer Use Case**: [POST] /concerts?offset=&limit=
        - **Input:**
            - `query`
            - `offset` (begin at 0)
            - `limit` (default 20)
        - **Process:**
            1. Query by region include isDeleted=false.
        - **Output:**
            - Page of concerts (preview info).

---
## Booking Service

### 1. Create Booking: [POST] /bookings

- **Required Role:** `customer`
- **Input:**
    - `booking_info`:
        - `items` (required)
- **Process:**
    1. Validate inputs.
    2. Determine current region (e.g. from `Accept-Language` header or other source).
    3. Determine current userId (from claim "sub" of JWT).
    4. Create new booking.
    5. Create items for new booking.
        - Rollback if failed: delete new booking
    6. (KAFKA) Send message to Concert Service for updating Seats status.
        - Rollback if failed at Concert Service: 
          - Delete new booking with all items.
          - Send notification (mail/websocket) to user.
- **Output:**
    - ID of new booking.

---

### 2. Update Booking: [PUT] /bookings

- **Required Role:** `customer` and isOwner=true
- **Input:**
    - `booking_info`:
        - `id` (required)
        - `new_items` (required)
        - `deleted_items` (required)
- **Process:**
    1. Validate inputs.
    2. Check if existed.
    3. Create booking item from `new_items`.
    4. Delete booking item from `deleted_items`.
    5. (KAFKA) Send message to Concert Service for updating Seats status.
        - Rollback if failed at Concert Service:
            - Delete new item of booking.
            - Create deleted item of booking.
            - Send notification (mail/websocket) to user.
- **Output:**
    - ID of updated booking.

---

### 2. Update Booking Status: [PATCH] /bookings

- **Required Role:** `organizer` and isOwnerOfConcert=true
- **Input:**
    - `booking_info`:
        - `id` (required)
        - `status` (PENDING|CONFIRMED|CANCELED)
- **Process:**
    1. Validate inputs.
    2. Check if existed.
    3. Update status for booking.
    4. Delete booking item from `deleted_items`.
    5. Send notification (mail/websocket) to user.
- **Output:**
    - ID of updated booking.

---

### 3. Delete Booking: [DELETE] /bookings

- **Required Role:** `customer` and isOwner=true
- **Input:**
    - `booking_id` (required)
- **Process:**
    1. Check if existed.
    2. Delete booking with all items.
    3. (KAFKA) Send message to Concert Service for updating Seats status.
       - Rollback if failed at Concert Service:
           - Create deleted booking with all items.
           - Send notification (mail/websocket) to user.
- **Output:**
    - ID of updated booking.

---

### 4. Booking Details
- **Variants:**
    - **Organizer Use Case**: [GET] /bookings/{id}
        - **Required Role:** `organizer` and isOwnerOfConcert=true
        - **Input:**
            - `booking_id` (required)
        - **Process:**
            1. Check if existed.
            2. Query by ID.
        - **Output:**
            - Details of booking (full info).

    - **Customer Use Case**: [GET] /bookings/{id}/owner
        - **Required Role:** `customer` and isOwner=true
        - **Input:**
            - `booking_id` (required)
        - **Process:**
            1. Check if region is matched.
            2. Check if existed.
            3. Query by ID.
        - **Output:**
            - Details of booking (full info).

---

### 5. Search Bookings

- **Variants:**
    - **Organizer Use Case**: [GET] /bookings/{id}/concert/{concertId}?offset=&limit=
        - **Required Role:** `organizer`
        - **Input:**
            - `query`
            - `offset` (begin at 0)
            - `limit` (default 20)
        - **Process:**
            1. Query by concert' s owner ID & region include isDeleted=true.
        - **Output:**
            - Page of bookings (preview info).

    - **Customer Use Case**: [GET] /bookings/me?offset=&limit=
        - **Required Role:** `customer`
        - **Input:**
            - `query`
            - `offset` (begin at 0)
            - `limit` (default 20)
        - **Process:**
            1. Query by owner ID & region include isDeleted=false.
        - **Output:**
            - Page of bookings (preview info).

---
## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/topics/git/add_files/#add-files-to-a-git-repository) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitsdc.tma.com.vn/ldnhan-batch48/concertify.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

- [ ] [Set up project integrations](https://gitsdc.tma.com.vn/ldnhan-batch48/concertify/-/settings/integrations)

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Set auto-merge](https://docs.gitlab.com/user/project/merge_requests/auto_merge/)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thanks to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
