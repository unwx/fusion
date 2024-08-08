package unwx.fusion.entity

data class Connection(
    val player1: ActivePlayer,
    val player2: ActivePlayer
) {
    fun partnerOf(player: ActivePlayer): ActivePlayer = when (player) {
        player1 -> player2
        player2 -> player1
        else -> throw IllegalArgumentException("Unrecognised player: '$player'. Known: ${players.joinToString()}")
    }

    val players: List<ActivePlayer> = listOf(player1, player2)
}
