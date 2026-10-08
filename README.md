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
- Supports single-elimination, double-elimination, and round-robin brackets for racket tournaments.
- Stores the tournament name, description, sport, bracket type, players, and format-specific rounds and winners.
- Registers players with skill levels and creates tournament matches automatically.
- Supports singles and doubles in the same tournament, with separate brackets and champions for each format; doubles teams are paired in registration order.
- Double elimination gives each entrant a losers-bracket path after one loss and uses a reset final if the undefeated finalist loses the grand final.
- Round robin schedules each entrant against every other entrant once; standings rank by wins, then point differential, then registration order.
- Handles uneven elimination brackets with byes; round-robin schedules give each entrant a bye round when needed.
- Records match results, advances winners, and updates tournament progress through the racket court workflow.
- Keeps tournament logic separate from the existing open-play queue workflow.

### Tournament bracket window

- Starts with a sport and mode selection popup.
- Racket plus Open Play opens the existing racket dashboard.
- Ball and Board remain selectable but show an unavailable message and stay on the selection popup.
- Racket plus Tournament opens a setup popup for the bracket name, bracket type, and description.
- After valid setup, opens the racket dashboard and tournament bracket window together.
- Add Singles and Doubles players from the dashboard player rows. In Doubles, checked-in players are paired into teams in registration order.
- Displays separate brackets for Singles and Doubles, with the selected bracket format shown in its own tab.
- The tournament window displays bracket matches, selected-match status, progress, and Round Robin standings; player registration and match queuing remain in the dashboard.
- Starting matches and recording scores use the racket dashboard's court controls; completed results advance the bracket.
- Prevents a second racket dashboard from being opened while an existing dashboard is active.

### Application flow

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
- `src/Management/TournamentManager.java` - Tournament registration, bracket progression, and result operations
- `src/tournament/StartupSelectionDialog.java` - Startup sport and mode selection
- `src/tournament/TournamentSetupDialog.java` - Tournament setup and validation
- `src/tournament/TournamentFrame.java` - Tournament bracket and match controls
- `src/tournament/BracketPanel.java` - Bracket renderer and match selection
- `src/tournament/Main.java` - Application startup and navigation entry point
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

## Requirements

- Windows
- JDK 17 or newer with `javac`, `jar`, and `jpackage` available on `PATH`

## Build the Windows Application

From the `SQS` directory, run:

```bat
build.bat
```

The script compiles the source files, creates `build/package-input/SQS.jar`, and generates a Windows executable in:

```text
dist/SQS-1.0.0.exe
```

## Run from Compiled Classes

After building the project, you can launch the application with:

```bat
java -cp build/classes tournament.Main
```

## Queue Rules

- A single match can include up to 2 players.
- A doubles match can include up to 4 players.
- Players are only matched when their format is the same.
- Players are considered compatible when skill difference is within 1 rank.
- A player in the on-hold list can be moved to the queue manually or automatically.
- Completed matches return all players to the on-hold list.
- Courts can only hold one active match at a time.
