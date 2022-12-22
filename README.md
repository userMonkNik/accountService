# accountService
## RESTfull app with role-based authorization. Stack: Spring Boot/Security/Gradle/Hibernate/H2/Stream API.

| Request                      | Anonymous | User | Accountant | Administrator | Auditor |
| ---------------------------- |:---------:|:----:|:----------:|:-------------:|:-------:|
| POST   api/auth/signup	     | +	       | +	  | +	         |+	             | +       |
| POST   api/auth/changepass	 | +	       | +    | +	         |-              | -       |
| GET    api/empl/payment	     | -         | +    | +	         |-	             | -       | 
| POST   api/acct/payments	   | -         | -    | +	         |-              | -       |
| PUT    api/acct/payments     | -         | -	  | +	         |-              | -       |
| GET    api/admin/user	       | -         | -	  | -	         |+              | -       | 
| DELETE api/admin/user	       | -         | -    | -	         |+	             | -       |
| PUT    api/admin/user/role	 | -         | -    | -	         |+	             | -       |
| PUT    api/admin/user/access | -         | -    | -	         |+	             | -       |
| GET    api/security/events	 | -         | -	  | -	         |-	             | +       |

### App endpoint's description:
* **POST   api/auth/signup** : Registration. Requires request body in json format with the following fields:
  * **name** (Cannot be empty)
  * **lastname** (Cannot be empty)
  * **email** (Must have corporate domain '@acme.com', cannot be empty)
  * **password** (Must be at least 12 chars)
* **POST   api/auth/changepass** : Changes password. Requires request body in json format with the following field:
  * **new_password** (Must be at least 12 chars)
* **GET    api/empl/payment** : 
  * Without parameters: Retrieves list of all employee's payments sorted by date(ASC) or empty list. 
  * With requested body in format 'mm-yyyy': Retrieves payment with stated date if exists. Month must be in 1-12 range.
* **POST   api/acct/payments** : Posts one or several payment entities. Uses transactional method, in case of error none of payments will be added. Requires request body in json list format with the following fields:
  * **employee** Employee's email (Cannot be empty)
  * **period** (Must be in format 'mm-yyyy', month range 1-12)
  * **salary** (Cannot be negative)
* **PUT    api/acct/payments** : Modifies payment entity. Requires request body in json format with the following fields:
  * **employee** Employee's email (Cannot be empty)
  * **period** (Must be in format 'mm-yyyy', month range 1-12)
  * **salary** (Cannot be negative)
* **GET    api/admin/user** : Retrieves list of all users sorted by id(DESC).
* **DELETE api/admin/user** : Deletes user with email stated as path variable.
* **PUT    api/admin/user/role** : Changes user role. Administrator cannot combine business role or delete administrator role. Requires request body in json format with the following fields:
  * **user** Employee's email (Cannot be empty)
  * **role** Role to grant or remove
  * **operation** (GRANT or REMOVE) case insensetive
* **PUT    api/admin/user/access** : Changes account's access. Cannot lock 'administrator' role. Requires request body in json format with the following fileds:
  * **user** Employee's email (Cannot be empty)
  * **operation** (LOCK or UNLOCK) case insensetive
* **GET    api/security/events** : Retrieves list of security log events or empty list.

Each new user except first registred has 'user' role. First registered user has 'administrator' role. App security has brute force defend mechanism. Account will be blocked in case of 5 incorrect attempts for 24 hours.
