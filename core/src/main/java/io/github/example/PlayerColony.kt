package io.github.example

class PlayerColony(
    var population: Int,
    var pawns: MutableList<PlayerPawn> = mutableListOf<PlayerPawn>(),
    var tasks: MutableList<Task> = mutableListOf<Task>(),
    var wealth: Int
) {

}
