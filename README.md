![logo](https://cdn.modrinth.com/data/cached_images/f382a695572536b8c14e53e50d4ace1f6b50dbca.png)
## About
A plugin designed to enhance the survival multiplayer experience by introducing the concept of guilds. This plugin allows players to form groups, fostering collaboration, competition, and social interaction within the game without ruining the vanilla experience.

## Features
### 1. **Role-Based Hierarchy**
- **Roles**: Guilds are structured with three core roles: **Master**, **Officer**, and **Journeyman**.
- Each role has distinct permissions for guandild management (e.g., promotion, invitation, or other similar actions).

### 2. **Unique Guild Identity**
- Every guild has a **unique tag** and **name** to differentiate it from others.
- Tags are displayed publicly in player interactions and serve as identifiers in guild-related commands.
- **Name uniqueness** ensures no conflicts or duplicates in the system.

### 3. **Customizable Message System**
- All system messages are fully **configurable** through the `messages.yml` file.
- Messages are categorized based on the recipient: one for the sender, one for the target, and one for other guild members.
- Internal placeholders allow dynamic content (e.g., guild names, player names) to be easily inserted into messages.
- Support for multiple message versions allows for personalized communication for different guild actions.

### 4. **Flexible Databases**
- Server administrators can choose between a **local database** or a **remote database** for storing guild data, configurable via the `config.yml` file.
- **Local database**: Simple, self-contained storage solution ideal for smaller servers or standalone setups.
- **Remote database**: Offers scalability and centralized storage, perfect for larger servers or networks that require consistent data across multiple locations.


## Documentation
For detailed information on commands, permissions, and general guidance on how the plugin operates, please refer to the [Wiki](https://github.com/slowmarc/GuildSystem/wiki).

## Disclaimer
This plugin is my first venture into Minecraft plugin development, and I recognize that there may be challenges and issues that arise as users interact with it. I genuinely appreciate your feedback and support!

If you encounter any bugs or have suggestions for improvements, please report them [here](https://github.com/slowmarc/GuildSystem/issues). Your contributions and insights will help enhance the plugin for everyone.

Additionally, this project is open source! Contributions are welcome, whether through code improvements, feature requests, or documentation enhancements.
