package solim.layout;

/**
 * Marker interface for Arc elements that carry Solim size constraints.
 * ATTACHERs check for this interface when adding a child to a cell, and apply
 * the stored constraints (prefWidth, minWidth, maxWidth, growX, etc.) to that cell.
 */
public interface ConstrainedElement {
	SizeConstraints getSizeConstraints();
}
