Home Screen Vault
---
A widget allowing you to browse note entries of a Obsidian vault, on home screen

<img src="Preview1.png" width="300px"></img>

### Notice

- This widget is responsive but some devices provides incorrect widget dimensions to the widget, in this case you need to set a width correction factor. Enable debug line in configuration and adjust the factor until the line is as long as the width of the widget.
- It won't observe file changes at realtime. It scans the vault periodically(period could be set, default is 60s); It also scans once you interact with it.
- After scanning 10 times observing no changes, it will cold down and slows the period of scanning(also could be set, default is 3600s).

- It is super wierd that we cannot read the files & folders in the vault, but only a ".obsidian", even after granting permissions using "Choose Folders" interface.
  so we'd have to do what our neighbour obsidian-todo-widget does --- sorry but you need to grant the permission of "All Files Access". Well I swear it will do nothing sensitive. Source code's just here.

- This widget is still not fully developed --- Actually it's my first time to make a Android project; There's still too much I don't know about Android. This work is still a beta of beta, with many parts still not following the best practices I've known or dont know for architecting a real project. (Why is it just so hard to determine a structure in Android???)

  If you found any strange design pattern in my codebase, like "Why don't you abstract out a data source?" or "Why do you put scanning work inside repository layer?" --- My response will all be "I don't know.""I'm not sure.""I haven't learned that yet." There should be hardly any architecture designs made with any so-called "underlying deep meaning". You are very very welcomed to help me point out where could be better redesigned/refactored. Also art/ui recommendations are welcomed too.

  Plus, probably I could hardly spare much effort on the enhancements of this project for some times to come.... This project really needs your valuable feedback, your valuable inspections and suggestions(on the structure of this project because I found Gemini is still not very smart), and even your valuable collaboration to be developed further. In a word this project really needs your help.


# Todo - Enhancements
- [ ] Add support for multiple vaults & multiple subforders, to be displayed by multiple widgets
- [ ] Start to use the unused state-checking methods
- Responsive design:
  - [ ] Combine Navigator and title when width is tight
  - [x] Smartly set the column numbers of file grid
- [ ] ```WidgetStateRepository``` thorough ```StateFlows```
- [ ] ```MainActivity``` fully adapt to flow of ```AppConfig```
- [ ] idk what else...refactor my spaghetti codes?

# Credits
- Gemini-Pro & Poe
- [Obsidian](https://obsidian.md/)
- [YukiGasai/obsidian-todo-widget](https://github.com/YukiGasai/obsidian-todo-widget) for some references










---

<sub><sub><sub><sub>but why, no one of the Obsidian users all over the world had even thought to made it.......</sub></sub></sub></sub>

<sub><sub><sub><sub>......</sub></sub></sub></sub>

*<sub><sub><sub><sub>ASCENSION MUST BE MADE. WHATEVER IT COSTS.</sub></sub></sub></sub>*
