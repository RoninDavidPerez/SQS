# SQS (Sports Queue System)

SQS is a Java Swing desktop application for managing player check-in, queue formation, court assignment, and live match tracking for a sports facility.

## Features

### Queue workflow

- Adds players by name, match format, and skill level.
- Tracks players in `OH_HOLD`, `IN_QUEUE`, and `IN_MATCH` states.
- Supports both `SINGLE` and `DOUBLE` match formats.
- Enforces a maximum player count of 2 for singles and 4 for doubles.
- Automatically groups compatible players into match queues using skill matching rules.
- Allows manual creation of a match from selected players.
- Sends players back to the on-hold list when a match is completed.

### Match-making and scheduling

- Manages up to four courts in a single system.
- Matches players by format and skill compatibility.
- Checks compatibility using a skill difference threshold of 1 rank.
- Creates a new pending match when a player cannot join an existing compatible match.
- Marks a match as ready when it reaches full capacity.
- Tracks queue and court status across the application.

### Court and match management

- Starts, pauses, ends, and clears matches on courts.
- Shows each court's current match, players, and timer countdown.
- Refreshes court timers every second while matches are running.
- Frees courts automatically when a match finishes.
- Allows queue and on-hold lists to be cleared or adjusted from the GUI.

### Player and match status tracking

- Tracks players using `PlayerStatus` values:
  - `OH_HOLD`
  - `IN_QUEUE`
  - `IN_MATCH`
- Tracks matches using `MatchStatus` values:
  - `PENDING`
  - `RUNNING`
  - `PAUSED`
  - `FINISHED`
- Stores match details including court assignment, format, player list, and score state.

### Dashboard customization

- Provides a Settings panel on the left side of the racket dashboard.
- Allows the administrator to change the match timer duration from 1 to 180 minutes.
- Allows the dashboard title and description to be changed.
- Displays the title and description centered above the court matches window.
- Shows a difficulty color indicator for every supported skill level.
- Uses matching difficulty colors in the Settings panel, On Hold cards, and Queue cards.
- Shows only the unique difficulty colors used by a queued match beside its match number.
- Keeps player names uncluttered in Queue cards while retaining difficulty tooltips on the color indicators.

### Tournament backend

- Provides a tournament model for racket tournaments.
- Supports single-elimination brackets as the first bracket type.
- Stores the tournament name, description, sport, bracket type, match format, players, rounds, and winner.
- Registers players and creates tournament matches automatically.
- Handles uneven player counts with byes.
- Records winners, advances them to the next round, and marks the tournament as completed.
- Keeps tournament logic separate from the existing open-play queue workflow.

## Planned Features

The following work is planned but is not fully connected to the application startup flow yet:

- Add a startup popup for selecting `Racket`, `Ball`, or `Board`.
- Add an `Open Play` or `Tournament` mode selection.
- Open the existing `MainGUI` directly for racket open play.
- Keep ball and board dashboards unavailable until their dashboards are developed.
- Add a tournament setup popup with bracket name, bracket type, and description fields.
- Open the racket dashboard and a fully functional tournament window after setup.
- Display tournament rounds, players, matches, results, and winner in the tournament window.
- Connect tournament player and match updates to the existing dashboard where appropriate.
- Add additional bracket types after single elimination is stable.

### Planned application flow

1. The program opens the sport and mode selection popup.
2. Racket plus Open Play opens the existing racket dashboard.
3. Ball or Board displays an unavailable message and returns to the selection popup.
4. Racket plus Tournament opens the tournament setup popup.
5. The player enters the bracket name, bracket type, and description.
6. Valid setup information creates the tournament.
7. The racket dashboard and tournament window open together.
8. Players are added, matches are generated, winners advance, and the final winner is displayed.

## Project Structure

- `src/` - Java source files
- `src/GUI/MainGUI.java` - Main Swing interface and application entry point
- `src/GUI/OnHoldPlayerCard.java` - On-hold player display card
- `src/GUI/PlayerRowPanel.java` - Player row component in the UI
- `src/GUI/QueueMatchCard.java` - Queue match display card
- `src/Collection/OnHoldList.java` - Player waiting list storage
- `src/Collection/QueueList.java` - Pending and ready match queue storage
- `src/Management/QueueService.java` - Queue lifecycle and player-to-match operations
- `src/Management/MatchMaker.java` - Compatibility and match creation logic
- `src/Management/CourtManager.java` - Court assignment and match control
- `src/Management/TournamentManager.java` - Racket tournament creation and bracket progression
- `src/Model/Player.java` - Player identity, format, status, and skill
- `src/Model/Match.java` - Match data, timer, scoring, and lifecycle
- `src/Model/Court.java` - Court state and assignment tracking
- `src/Model/MatchFormat.java` - Supported match formats
- `src/Model/MatchStatus.java` - Match lifecycle states
- `src/Model/PlayerStatus.java` - Player lifecycle states
- `src/Model/SkillLevel.java` - Skill ranking values
- `src/Model/BracketType.java` - Supported tournament bracket types
- `src/Model/Sport.java` - Supported sport selections
- `src/Model/Tournament.java` - Tournament details, players, rounds, and winner
- `src/Model/TournamentMatch.java` - Tournament match and advancement state
- `src/Model/TournamentStatus.java` - Tournament lifecycle states
- `src/GUI/CourtWindow.java` - Separate court matches window and centered dashboard header
- `build.bat` - Compiles the project and packages the Windows app
- `build/` - Compiler output generated during the build
- `dist/` - Generated desktop application output

## Requirements

- Windows
- JDK 14 or newer with `javac`, `jar`, and `jpackage` available on `PATH`

## Build the Windows Application

From the `SQS` directory, run:

```bat
build.bat
```

The script compiles the source files, creates `build/package-input/SQS.jar`, and generates a Windows executable in:

```text
dist/SQS/SQS.exe
```

## Run from Compiled Classes

After building the project, you can launch the application with:

```bat
java -cp build/classes GUI.MainGUI
```

## Queue Rules

- A single match can include up to 2 players.
- A doubles match can include up to 4 players.
- Players are only matched when their format is the same.
- Players are considered compatible when skill difference is within 1 rank.
- A player in the on-hold list can be moved to the queue manually or automatically.
- Completed matches return all players to the on-hold list.
- Courts can only hold one active match at a time.

## Branch Naming

Examples:

- `feature/player-queue`
- `ui-court-management`
- `feature/match-making`

## Team Responsibilities

### Santarin, Francos - Frontend / GUI

- Build the startup sport and mode selection popup.
- Add the Racket, Ball, and Board choices.
- Add the Open Play and Tournament choices.
- Add the continue action and connect it to the next workflow step.

### Andres, Jerald - Backend

- Control startup navigation based on the selected sport and mode.
- Open the existing racket dashboard for Open Play.
- Block unsupported Ball and Board dashboard paths.
- Prevent duplicate dashboard windows.

### Bagagnan, Aldrin - Backend / GUI

- Build the tournament window layout.
- Display tournament rounds and matches.
- Add controls for players, starting matches, recording results, and showing progress.

### Ebonia, Tristan - Frontend

- Build the tournament setup popup.
- Add bracket name, bracket type, and description fields.
- Add validation and keep the popup styling consistent with the existing dashboard.

### Flores, Ryiogi Zen - Backend / Collection / Model / Management

- Extend the tournament model and collections as the tournament UI requires.
- Maintain players, matches, rounds, and tournament statuses.
- Support bracket generation, winner advancement, and match result updates.

### Lagoras, Rus - Fullstack

- Connect the tournament setup popup, tournament backend, and tournament window.
- Keep tournament data synchronized with the dashboard.
- Coordinate integration between frontend and backend changes.

### Ronin David Perez - Project lead

- Decide supported bracket types and sport behavior.
- Confirm that racket is the first supported sport.
- Review and merge member contributions.
- Resolve integration issues between GUI and backend.
- Run the complete workflow test before release.

