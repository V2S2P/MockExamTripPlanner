[JPA_Helper_Methods_Explanation.md](https://github.com/user-attachments/files/23209531/JPA_Helper_Methods_Explanation.md)

# Understanding Helper Methods in JPA Bidirectional Relationships

This document explains **when and why to use helper methods** in JPA entity relationships, using examples from the `User`–`Role` (Many-to-Many) and `Guide`–`Trip` (One-to-Many) relationships.

---

## 🧩 The Core Idea

In JPA, there are **two worlds**:

1. **Java objects in memory**
2. **Database rows in tables**

When you have bidirectional relationships (where two entities reference each other), you need to keep **both sides consistent** in Java memory — not just in the database.

---

## ⚙️ Example 1: User ↔ Role (Many-to-Many)

### Entity setup

```java
// User.java
@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_name"),
    inverseJoinColumns = @JoinColumn(name = "role_name")
)
private Set<Role> roles = new HashSet<>();
```

```java
// Role.java
@ManyToMany(mappedBy = "roles")
private Set<User> users = new HashSet<>();
```

### The problem

If you do:

```java
alice.getRoles().add(admin);
```

then only the **User** knows about the relationship:

```
Alice.roles = [Admin]
Admin.users = []
```

That’s inconsistent in memory.  

### The fix — helper method

```java
public void addRole(Role role) {
    if (role == null) return;
    roles.add(role);              // Add role to user
    role.getUsers().add(this);    // Add user to role
}
```

Now both sides agree:

```
Alice.roles = [Admin]
Admin.users = [Alice]
```

This keeps your in-memory model consistent, preventing bugs and ensuring correct persistence.

---

## 🧭 Example 2: Guide ↔ Trip (One-to-Many / Many-to-One)

### Entity setup

```java
// Guide.java
@OneToMany(mappedBy = "guide", cascade = CascadeType.ALL)
private List<Trip> trips;
```

```java
// Trip.java
@ManyToOne
private Guide guide;
```

Here, only `Trip` is the **owning side** (it stores the foreign key).

So when you do:

```java
trip.setGuide(guide);
em.persist(trip);
```

JPA already knows how to save the relationship.  
You don’t need to update both sides.

### Optional helper (for consistency only)

```java
public void addTrip(Trip trip) {
    trips.add(trip);
    trip.setGuide(this);
}
```

This helps keep Java objects consistent in memory but isn’t required for persistence.

---

## 🧠 Summary

| Relationship Type | Both sides have collections? | Helper needed? | Why |
|--------------------|-----------------------------|----------------|-----|
| `@ManyToMany`      | ✅ Yes                      | ✅ Yes (recommended) | Both sides must stay in sync in memory |
| `@OneToMany` / `@ManyToOne` | ❌ No | ❌ Not required (optional) | Only one side owns the relationship; JPA handles it |

---

## ✅ TL;DR

- Use helper methods when **both sides have collections** (like `User` ↔ `Role`).
- They ensure both sides of the relationship are consistent **in memory**.
- They’re **not needed** when only one side has a list (like `Guide` ↔ `Trip`).

Perfect rule of thumb:
> "If both sides can hold multiple references (Many-to-Many), add helpers.
> If only one side has a list (One-to-Many), it’s optional."
[RestAssured_Testing_Guide.md](https://github.com/user-attachments/files/23270467/RestAssured_Testing_Guide.md)

# REST Assured Testing with Hamcrest

## 🧩 What is REST Assured?
**REST Assured** is a Java library used to test RESTful APIs. It simplifies sending HTTP requests and validating responses, making it ideal for **integration testing** in Java web applications.

In this project, REST Assured is used to verify that endpoints for `/guides`, `/trips`, and `/packing` behave correctly — returning the right **status codes** and **JSON responses**.

Example:
```java
given()
    .header("Authorization", "Bearer " + userToken)
    .when()
    .get("/trips")
    .then()
    .statusCode(200)
    .body("size()", greaterThanOrEqualTo(1));
```

This test sends a `GET /trips` request and checks that the response status is **200 OK** and that at least one trip exists.

---

## 🧩 What is Hamcrest?
**Hamcrest** is a library for writing **readable and flexible assertions** in Java tests. It provides matchers like `equalTo`, `greaterThan`, `hasItems`, and `notNullValue`, which make test conditions expressive and easy to understand.

Hamcrest is used inside REST Assured’s `.body()` and `.statusCode()` checks to make assertions clear and descriptive.

Example:
```java
.body("name", equalTo("Alice Johnson"))
.body("packingItems.name", hasItems("Tent", "Backpack"))
.body("packingItems[0].weightInGrams", greaterThan(0));
```

Here, Hamcrest helps express that:
- the trip name equals “Alice Johnson”
- the packing list includes “Tent” and “Backpack”
- each packing item has a positive weight

---

## ✅ Example: Mock API Testing
The test mode in this project uses a **MockPackingService** instead of the real API to ensure consistent results:

```java
@Test
void testPackingEndpoints() {
    given()
        .header("Authorization", "Bearer " + userToken)
        .when()
        .get("/trips/" + createdTripId + "/packing")
        .then()
        .statusCode(200)
        .body("packingItems.size()", equalTo(2))
        .body("packingItems.name", hasItems("Tent", "Backpack"));

    given()
        .header("Authorization", "Bearer " + userToken)
        .when()
        .get("/trips/" + createdTripId + "/packing/weight")
        .then()
        .statusCode(200)
        .body(equalTo("3300"));
}
```

This test verifies that the mock service returns two predictable items (`Tent`, `Backpack`) with a total weight of **3300 grams**.

---

## 💡 Why Use Them Together?
Using **REST Assured** with **Hamcrest** allows for:
- Fluent and human-readable tests
- Reliable verification of JSON responses
- Consistent regression testing of REST APIs

Together, they ensure that your endpoints behave as expected and that future changes don’t break existing functionality.
[ExceptionHandling_Readme.md](https://github.com/user-attachments/files/23308401/ExceptionHandling_Readme.md)# 🧩 Exception Handling Overview

This document explains the purpose and usage of the custom exceptions used in the project.

---

## 🧠 Overview of Exceptions

| Exception | Inherits from | Typical HTTP Code | Purpose |
|------------|---------------|------------------|----------|
| **`ApiException`** | `RuntimeException` | varies (400–500) | Generic catch-all for API-related errors; used to send standardized JSON errors. |
| **`ValidationException`** | `RuntimeException` | `400 Bad Request` | Thrown when user input fails validation (e.g. missing fields, bad format). |
| **`NotAuthorizedException`** | `RuntimeException` | `401 Unauthorized` or `403 Forbidden` | Thrown when user authentication/authorization fails. |
| **`EntityNotFoundException`** | `Exception` | `404 Not Found` | Thrown when requested data (entity) doesn’t exist in the database. |

---

## 🔹 1. `ApiException`

**Purpose:**  
A general-purpose exception that lets you control both the HTTP status code and message returned to the client.

**When to use:**  
When you need to throw a meaningful error to the API client, with a specific status code and message.

**Example:**
```java
if (customer == null) {
    throw new ApiException(404, "Customer not found");
}
```

**Handled by:**  
Your global exception handler in `ApplicationConfig`:

```java
app.exception(ApiException.class, (e, ctx) -> {
    ctx.status(e.getStatusCode()).json(Map.of(
        "status", e.getStatusCode(),
        "message", e.getMessage()
    ));
});
```

---

## 🔹 2. `ValidationException`

**Purpose:**  
Used to indicate that the user’s input was invalid (HTTP 400).

**When to use:**  
When you’re validating input data and something doesn’t meet your criteria.

**Example:**
```java
if (carDTO.getMaker() == null || carDTO.getMaker().isBlank()) {
    throw new ValidationException("Car maker cannot be empty");
}
```

**Best practice:**  
Catch `ValidationException` inside your service or controller and wrap it in an `ApiException` with HTTP 400:

```java
catch (ValidationException e) {
    throw new ApiException(400, e.getMessage());
}
```

---

## 🔹 3. `NotAuthorizedException`

**Purpose:**  
Signals that a user tried to perform an action they’re not allowed to do (401 or 403).

**When to use:**  
In authentication/authorization logic (like your `SecurityController` or protected service methods).

**Example:**
```java
if (!user.hasRole(Roles.ADMIN)) {
    throw new NotAuthorizedException(403, "Only admins can delete cars");
}
```

**Handled by:**  
Can have its own handler, or be wrapped into `ApiException`.

```java
app.exception(NotAuthorizedException.class, (e, ctx) -> {
    ctx.status(e.getStatusCode()).json(Map.of(
        "status", e.getStatusCode(),
        "message", e.getMessage()
    ));
});
```

---

## 🔹 4. `EntityNotFoundException`

**Purpose:**  
Thrown when something is requested from the database but doesn’t exist.  
This is a checked exception, so it must be declared with `throws`.

**Example:**
```java
public Car getById(int id) throws EntityNotFoundException {
    Car car = em.find(Car.class, id);
    if (car == null) {
        throw new EntityNotFoundException("Car with id " + id + " not found");
    }
    return car;
}
```

In the service layer:
```java
try {
    return carDAO.getById(id);
} catch (EntityNotFoundException e) {
    throw new ApiException(404, e.getMessage());
}
```

---

## 🧭 Summary — When to Use What

| Exception | Layer | Trigger Condition | Typical Handling |
|------------|--------|------------------|------------------|
| **`EntityNotFoundException`** | DAO | Database lookup fails | Caught in Service → wrap in `ApiException(404, msg)` |
| **`ValidationException`** | Service / Controller | Invalid input | Caught in Service → wrap in `ApiException(400, msg)` |
| **`NotAuthorizedException`** | Security / Controller | Unauthorized access | Global handler or wrap in `ApiException(403, msg)` |
| **`ApiException`** | Any layer | General API error | Handled globally (directly returned as JSON) |

---

## 🧠 Best Practice Tip

If you want a *single* consistent flow, you can choose to throw only `ApiException` from your service layer and use the other three internally — always wrapped into `ApiException`.

✅ Clean, predictable JSON responses  
✅ No try/catch clutter in controllers  
✅ Semantic clarity in your internal logic

[JPA_CascadeType_FetchType_Notes.md](https://github.com/user-attachments/files/23308860/JPA_CascadeType_FetchType_Notes.md)
# JPA Relationship Notes: CascadeType and FetchType

## 🧩 CascadeType

### 🔹 What it is
`CascadeType` defines how certain operations performed on one entity should also be applied (or *cascaded*) to its related entities.

Think of it like a “chain reaction”:  
If you perform an operation (like `persist`, `remove`, etc.) on a **parent entity**, Hibernate will automatically apply that same operation to the **child entities** — depending on the cascade type.

### 🔹 Example
```java
@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
private List<Car> cars = new ArrayList<>();
```
When you perform an operation on a `Customer`, the same operation will automatically be applied to all `Car` entities that belong to that customer.

Example:
```java
Customer customer = new Customer();
Car car = new Car();
customer.getCars().add(car);
car.setCustomer(customer);

// Because of CascadeType.ALL:
em.persist(customer); // ✅ will also persist the car automatically
em.remove(customer);  // ✅ will also remove all their cars automatically
```

### 🔹 Common Cascade Types

| Cascade Type | Description |
|---------------|-------------|
| `PERSIST` | When the parent is persisted, the child is also persisted. |
| `MERGE` | When the parent is merged (updated), the child is merged too. |
| `REMOVE` | When the parent is deleted, the child is deleted too. |
| `REFRESH` | When the parent is refreshed from the database, the child is refreshed too. |
| `DETACH` | When the parent is detached from the persistence context, the child is detached too. |
| `ALL` | Applies **all** the above operations. |

### ⚠️ Important Note
Use cascading carefully:
- It’s great for **ownership** relationships (like `Customer → Cars`).
- Avoid for **shared** entities (e.g., `Order → Product`), since removing one could delete the shared entity.

---

## 🧠 FetchType

### 🔹 What it is
`FetchType` defines **when** related entities are loaded from the database — either **immediately** or **only when accessed**.

It controls *how much data Hibernate fetches upfront.*

### 🔹 Two Types

| Fetch Type | Description | Use Case |
|-------------|--------------|----------|
| `EAGER` | Loads the related entity **immediately** when the parent is loaded. | Good for small, always-needed relationships. |
| `LAZY` | Loads the related entity **only when it’s accessed** (on demand). | Best for performance — avoids unnecessary joins. |

### 🔹 Example
```java
@ManyToOne(fetch = FetchType.LAZY)
private Customer customer;
```
When you fetch a `Car`, Hibernate **does not** immediately fetch the related `Customer` — it only loads the `Customer` when you call something like `car.getCustomer()`.

### 🔹 Default Fetch Behavior in JPA

| Relationship Type | Default Fetch Type |
|--------------------|--------------------|
| `@ManyToOne` | `EAGER` |
| `@OneToMany` | `LAZY` |
| `@OneToOne` | `EAGER` |
| `@ManyToMany` | `LAZY` |

In your case, you’ve **explicitly overridden** `@ManyToOne` to be `LAZY`, which is a **good practice** — since customers can have many cars, and you don’t want to always load the customer when you fetch a car.

---

## ✅ Summary

| Concept | Purpose | Example |
|----------|----------|----------|
| **CascadeType** | Controls whether entity operations (like persist, remove) propagate from parent to child. | `cascade = CascadeType.ALL` |
| **FetchType** | Controls when related entities are loaded from DB (immediately or on demand). | `fetch = FetchType.LAZY` |
