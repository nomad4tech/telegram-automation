# Telegram Automation
## Overview
* **Purpose:** The **telegram-automation** project aims to create automation for interacting with the Telegram messaging
  service using web browser drivers and other unofficial tools (not part of the Telegram API).
* **Features:**
    - Login to Telegram using phone number.
    - Support for custom code input methods via the `CodeProvider` interface.
    - Retains browser context for easier login sessions using the `ContextProvider` interface.
* **Limitations:** Currently, the project supports only the login functionality; further features are in development.
* **Warning:** Automating Telegram using unofficial methods, such as web drivers, may violate the service's terms of use and lead to account suspension. Telegram is actively developing its API, and using official tools is recommended for long-term stability and security of your project.
---
## Getting Started
### Prerequisites:
    - Java 17 or higher
    - Maven
    - Playwright (follow the instructions below for installation)
### Installation:
1. Install Playwright ([Installation Instruction](https://playwright.dev/docs/intro)):
  ``` bash 
   npm install playwright
  ```
2. Add the following dependency to your `pom.xml`:
  ``` xml
  <dependency>
      <groupId>tech.nomad4</groupId>
      <artifactId>telegram-automation</artifactId>
      <version>1.0-SNAPSHOT</version>
  </dependency>
  ```
3. Also, you can clone project source and install it in maven:
  ``` bash 
   git clone https://github.com/nomad4tech/telegram-automation.git
   cd telegram-automation
   npm clean install
  ```
### Usage:
#### TelegramLogin:
To create and configure `TelegramLogin`, use the following constructor:
  ``` java
  public TelegramLogin(TelegramLoginConfig config)
  ```
**Example Usage:**
  ``` java
  TelegramLoginConfig config = new TelegramLoginConfig(); 
  TelegramLogin telegramLogin = new TelegramLogin(config); 
  Page loggedInPage = telegramLogin.login("your_phone_number", browser);
  ```
**Advanced Usage:**
- **Configuration:** The `TelegramLoginConfig` class allows you to specify custom implementations for code input and
  context storage.
- **Customization:** Users can implement their own `CodeProvider` and `ContextProvider` interfaces to tailor the login
  process and context storage.
---
## Future Development
**Roadmap:** Planned features include support for additional automation tasks within Telegram, such as bot-like
interactions.
---
## Contributing
**Guidelines:** Contributions are welcome! Please fork the repository, make your changes, and submit pull request with description of your modifications.  
For questions or collaboration, feel free to reach out at [alex.sav4387@gmail.com](mailto:alex.sav4387@gmail.com), check out my **GitHub** profile: [nomad4tech](https://github.com/nomad4tech), or connect with me on **Telegram**: [@nomad4tech](https://t.me/nomad4tech).
---
## License
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

**Terms:** This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.