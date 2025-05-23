# Essentials Mod (1.18.2-Forge)

Welcome to the **Essentials Mod (1.18.2-Forge)**! This mod adds a collection of useful commands to enhance your Minecraft experience. Below is a detailed list of all the available commands and their functionalities.

---

## Data Storage

The data will be saved in the following file:
```
rootDirectory/config/essentials/config.yml
```

---

## Commands Documentation

### **```/adminsword```**
This command gives you an Admin Sword. *(Only Admins)*

---

### **```/back```**
This command teleports you back to your last death location.

---

### **```/day```**
This command sets the time to day. *(Only Admins)*

---

### **```/night```**
This command sets the time to night. *(Only Admins)*

---

### **```/delhome <optional Home Name>```**
This command removes the specified home. If no home name is provided, it removes the default home.

---

### **```/delwarp <Warp Name>```**
This command allows players to remove a warp location. *(Only Admins)*

---

### **```/enderchest <optional PlayerName>```**
This command opens the Ender Chest of the specified player or your own if no player name is provided.
*(Only Admins)*
---

### **```/feed <optional PlayerName>```**
This command restores the hunger level of the specified player or yourself if no player name is provided. *(Only Admins)*

---

### **```/fly <optional PlayerName>```**
This command toggles the ability to fly for the specified player or yourself if no player name is provided. *(Only Admins)*

---

### **```/fly <PlayerName> <Speed>```**
This command sets the fly speed for the specified player *(Only Admins)*

---

### **```/heal <optional PlayerName>```**
This command restores health for the specified player or yourself if no player name is provided. *(Only Admins)*

---

### **```/home <optional Home Name>```**
This command teleports you to the specified home location or your default home if no name is provided.

---

### **```/invsee <PlayerName>```**
This command lets you view and manage another player's inventory.
*(Only Admins)*
---

### **```/mute <PlayerName>```**
This command mutes the chat messages of the specified player. *(Only Admins)*

---

### **```/muteother <PlayerName>```**
This command mutes the chat messages from the selected player for you. **(Only Admins)**

---

### **```/gm <gameMode> <optional PlayerName>```**
This command changes the game mode for the specified player or yourself if no player name is provided. *(Only Admins)*

---

### **```/repair <optional PlayerName>```**
This command repairs the item held by the specified player or yourself if no player name is provided. *(Only Admins)*

---

### **```/sethome <optional Home Name>```**
This command sets a home location for you with the specified name or as the default if no name is provided.

---

### **```/setspawn```**
This command sets the world spawn point. *(Only Admins)*

---

### **```/setwarp <Warp Name>```**
This command sets a warp location with the specified name. *(Only Admins)*

---

### **```/spawn```**
This command teleports you to the world spawn location.

---

### **```/tpa <PlayerName|accept|deny>```**
This command allows players to request teleportation to another player and accept or deny incoming requests.

---

### **```/tpahere <PlayerName|accept|deny>```**
This command allows the current player to request teleportation to you and accept or deny incoming requests.

---

### **```/vanish <optional PlayerName>```**
This command toggles the invisibility of the specified player or yourself if no player name is provided. *(Only Admins)*

---

### **```/warp <Warp Name>```**
This command teleports you to a predefined warp location.

---

### **```/god <optional PlayerName>```**
This command makes you invulnerable or a specific Player

---

### **```/backpack```**
This command allows you to use the /backpack command
This Command is by default disabled in the Config

---

### **```/maintenance```**
This command enables the maintenance mode for the server
Only players who have been added to the List with the command /maintenance <PlayerName>
*(Only Admins)*

---

### **```/maintenance <PlayerName>```**
This command adds the player to the allowed List to join the server in maintenance mode.
*(Only Admins)*

---

## Build by yourself

``` bash
git clone https://github.com/frame-dev/EssentialsMod-1.20.1-Forge.git
cd EssentialsMod-1.20.1-Forge
./gradlew jar
```

The Jar is located in the build/libs directory.

## Installation

1. Download the **Essentials Mod (1.20.1)** JAR file [Latest Version Download](https://github.com/frame-dev/EssentialsMod-1.20.1-Forge/releases/latest).
2. Place the JAR file in the `mods` folder of your Minecraft installation directory.
3. Start Minecraft with Forge installed.

---

## Support

If you encounter any issues or have suggestions, feel free to contact us or submit an issue on our GitHub repository.

Enjoy the enhanced Minecraft experience with Essentials Mod!