### New Features

* **`/logout` command**
  Players can now reset their authentication session manually.

* **Alt account detection**
  Added an administrative command:

  ```
  /alts <player>
  ```

  This command allows administrators to search for alternative accounts.
  You can also limit the number of alt accounts in the configuration.

* **Additional account protection**
  New commands for extra security and recovery:

  ```
  /email
  /secret
  ```

  Players can now set an email and a secret question to recover their account.

---

### Improvements

* **Hybrid authentication fixed**
  Players will now always use the **offline UUID**, fixing issues related to hybrid authentication.

* **Social network linking feedback**
  When a player links a social account, they will instantly receive information about their account in private messages.

* **Server switching stability**
  Fixed an issue with fast server switching.
  The plugin will now first locate the correct **authentication or lobby server**, and only then connect the player.

* **Plugin size significantly reduced**
  The overall plugin size has been greatly optimized.

---

### Fixes

* Numerous minor bug fixes and stability improvements across the plugin.

---

