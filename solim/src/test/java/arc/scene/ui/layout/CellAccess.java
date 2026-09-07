package arc.scene.ui.layout;

/** Helper for unit testing package-private Arc Cell padding fields. */
public final class CellAccess {
	private CellAccess() {}

	public static float padTop(Cell<?> cell) {
		return cell.padTop;
	}

	public static float padLeft(Cell<?> cell) {
		return cell.padLeft;
	}

	public static float padBottom(Cell<?> cell) {
		return cell.padBottom;
	}

	public static float padRight(Cell<?> cell) {
		return cell.padRight;
	}
}
