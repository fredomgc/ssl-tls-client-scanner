package cz.ondrejsmetak.tool;

/**
 * Pair containing key and value
 *
 * @author Ondřej Směták <posta@ondrejsmetak.cz>
 * @param <K> key
 * @param <V> value
 */
public class Pair<K, V> {

	/**
	 * Key
	 */
	private final K left;

	/**
	 * Value
	 */
	private final V right;

	/**
	 * Creates a new pair with given key and value
	 *
	 * @param <K> data type of key
	 * @param <V> data type of value
	 * @param left key
	 * @param right value
	 * @return a newly created pair
	 */
	public static <K, V> Pair<K, V> createPair(K left, V right) {
		return new Pair<>(left, right);
	}

	/**
	 * Creates a new pair with given key and value
	 *
	 * @param left
	 * @param right
	 */
	public Pair(K left, V right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns "left" object, also reffered as "key"
	 *
	 * @return key
	 */
	public K getLeft() {
		return left;
	}

	/**
	 * Returns "right" object, also reffered as "value"
	 *
	 * @return value
	 */
	public V getRight() {
		return right;
	}

}
