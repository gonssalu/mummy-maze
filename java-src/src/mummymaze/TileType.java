package mummymaze;

public enum TileType {
    HERO('H'),
    V_WALL('|'),
    H_WALL('-'),
    EMPTY('.'),
    OPEN(' '),
    EXIT('S'),
    WHITE_MUMMY('M'),
    RED_MUMMY('V'),
    TRAP('A'),
    SCORPION('E'),
    KEY('C'),
    H_DOOR_OPEN('_'),
    H_DOOR_CLOSED('='),
    V_DOOR_OPEN('"'),
    V_DOOR_CLOSED('(');

    private final char identifier;

    TileType(char identifier) {
        this.identifier = identifier;
    }

    public char getIdentifier() {
        return identifier;
    }

    public static TileType getTileType(char identifier) {
        for (TileType tileType : TileType.values())
            if (tileType.getIdentifier() == identifier)
                return tileType;
        throw new IllegalArgumentException("No tile type was found with that identifier.");
    }

    public static boolean allowsHeroMovement(TileType tileType) {
        return (tileType != TileType.WHITE_MUMMY && tileType != TileType.RED_MUMMY &&
                tileType != TileType.TRAP && tileType != TileType.SCORPION);
    }

    public static boolean canVerticallyPass(TileType tileType) {
        return (tileType != TileType.H_WALL && tileType != TileType.H_DOOR_CLOSED);
    }

    public static boolean canHorizontallyPass(TileType tileType) {
        return (tileType != TileType.V_WALL && tileType != TileType.V_DOOR_CLOSED);
    }

    public static boolean isTileRelevantForHeuristic(TileType tileType) {
        return (tileType == TileType.H_DOOR_CLOSED || tileType == TileType.V_DOOR_CLOSED);
    }

    public static boolean shouldSaveFloorType(TileType tileType){
        return (tileType == TileType.KEY || tileType == TileType.TRAP);
    }

    public static boolean isMummy(TileType tileType) {
        return (tileType == TileType.WHITE_MUMMY || tileType == TileType.RED_MUMMY);
    }
}
