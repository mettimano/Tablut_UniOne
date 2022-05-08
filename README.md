# Tablut_UniOne for tablut_challenge_2022


Project for the [Tablut Competition](http://ai.unibo.it/games/boardgamecompetition/tablut),
The project realizes a player for an ancient Nordic strategy board game: Tablut.


## Requirements

To run this application a working Java environment is required. Software is tested on Java version 8.
To install Java in your Ubuntu/Debian machine you can execute the following command:

```bash
sudo apt install openjdk-8-jdk
```

## Usage

You can run the player with:
```bash
cd Executables
java -jar UniOne.jar <WHITE or BLACK> [<timeout>] [<server_address>]
```

If not specified, the default values are **60** for `timeout` and **localhost** for `server_address`.

You can run the player with the bash script provided:

```bash
./runmyplayer <WHITE or BLACK> [<timeout>] [<server_address>]
```

You can find more information in the [main repository](https://github.com/AGalassi/TablutCompetition)

## Authors

Alessandro Mettimano, Rabin Sikder
