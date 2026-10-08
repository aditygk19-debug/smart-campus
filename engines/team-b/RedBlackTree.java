/** Red-Black Tree routing table keyed by RoutingEntry.getKey(). */
public class RedBlackTree {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private class Node {
        RoutingEntry entry;
        Node left;
        Node right;
        Node parent;
        boolean color;

        Node(RoutingEntry entry, boolean color) {
            this.entry = entry;
            this.color = color;
        }
    }

    private final Node nil;
    private Node root;
    private int size;

    public RedBlackTree() {
        nil = new Node(null, BLACK);
        nil.left = nil;
        nil.right = nil;
        nil.parent = nil;
        root = nil;
        size = 0;
    }

    public int size() {
        return size;
    }

    public void insert(RoutingEntry entry) {
        if (entry == null || entry.getKey() == null) {
            throw new IllegalArgumentException("Routing entry and key are required.");
        }

        Node parent = nil;
        Node current = root;
        while (current != nil) {
            parent = current;
            int comparison = entry.getKey().compareTo(current.entry.getKey());
            if (comparison == 0) {
                current.entry = entry;
                return;
            }
            current = comparison < 0 ? current.left : current.right;
        }

        Node added = new Node(entry, RED);
        added.left = nil;
        added.right = nil;
        added.parent = parent;

        if (parent == nil) {
            root = added;
        } else if (entry.getKey().compareTo(parent.entry.getKey()) < 0) {
            parent.left = added;
        } else {
            parent.right = added;
        }

        size++;
        insertFixup(added);
    }

    private void insertFixup(Node node) {
        while (node.parent.color == RED) {
            if (node.parent == node.parent.parent.left) {
                Node uncle = node.parent.parent.right;
                if (uncle.color == RED) {
                    node.parent.color = BLACK;
                    uncle.color = BLACK;
                    node.parent.parent.color = RED;
                    node = node.parent.parent;
                } else {
                    if (node == node.parent.right) {
                        node = node.parent;
                        rotateLeft(node);
                    }
                    node.parent.color = BLACK;
                    node.parent.parent.color = RED;
                    rotateRight(node.parent.parent);
                }
            } else {
                Node uncle = node.parent.parent.left;
                if (uncle.color == RED) {
                    node.parent.color = BLACK;
                    uncle.color = BLACK;
                    node.parent.parent.color = RED;
                    node = node.parent.parent;
                } else {
                    if (node == node.parent.left) {
                        node = node.parent;
                        rotateRight(node);
                    }
                    node.parent.color = BLACK;
                    node.parent.parent.color = RED;
                    rotateLeft(node.parent.parent);
                }
            }
        }
        root.color = BLACK;
    }

    private void rotateLeft(Node node) {
        Node pivot = node.right;
        node.right = pivot.left;
        if (pivot.left != nil) {
            pivot.left.parent = node;
        }
        pivot.parent = node.parent;
        if (node.parent == nil) {
            root = pivot;
        } else if (node == node.parent.left) {
            node.parent.left = pivot;
        } else {
            node.parent.right = pivot;
        }
        pivot.left = node;
        node.parent = pivot;
    }

    private void rotateRight(Node node) {
        Node pivot = node.left;
        node.left = pivot.right;
        if (pivot.right != nil) {
            pivot.right.parent = node;
        }
        pivot.parent = node.parent;
        if (node.parent == nil) {
            root = pivot;
        } else if (node == node.parent.right) {
            node.parent.right = pivot;
        } else {
            node.parent.left = pivot;
        }
        pivot.right = node;
        node.parent = pivot;
    }

    public RoutingEntry search(String key) {
        if (key == null) {
            return null;
        }
        Node current = root;
        while (current != nil) {
            int comparison = key.compareTo(current.entry.getKey());
            if (comparison == 0) {
                return current.entry;
            }
            current = comparison < 0 ? current.left : current.right;
        }
        return null;
    }

    public RoutingEntry search(String key, String currentTopologyVersion) {
        RoutingEntry entry = search(key);
        if (entry == null || entry.isExpired()) {
            return null;
        }
        if (currentTopologyVersion == null
                || !currentTopologyVersion.equals(entry.getTopologyVersion())) {
            return null;
        }
        return entry;
    }

    public void remove(String key) {
        Node target = findNode(key);
        if (target == nil) {
            return;
        }

        Node moved = target;
        boolean originalColor = moved.color;
        Node fixNode;

        if (target.left == nil) {
            fixNode = target.right;
            transplant(target, target.right);
        } else if (target.right == nil) {
            fixNode = target.left;
            transplant(target, target.left);
        } else {
            moved = minimum(target.right);
            originalColor = moved.color;
            fixNode = moved.right;
            if (moved.parent == target) {
                fixNode.parent = moved;
            } else {
                transplant(moved, moved.right);
                moved.right = target.right;
                moved.right.parent = moved;
            }
            transplant(target, moved);
            moved.left = target.left;
            moved.left.parent = moved;
            moved.color = target.color;
        }

        size--;
        if (originalColor == BLACK) {
            deleteFixup(fixNode);
        }
    }

    private void deleteFixup(Node node) {
        while (node != root && node.color == BLACK) {
            if (node == node.parent.left) {
                Node sibling = node.parent.right;
                if (sibling.color == RED) {
                    sibling.color = BLACK;
                    node.parent.color = RED;
                    rotateLeft(node.parent);
                    sibling = node.parent.right;
                }
                if (sibling.left.color == BLACK && sibling.right.color == BLACK) {
                    sibling.color = RED;
                    node = node.parent;
                } else {
                    if (sibling.right.color == BLACK) {
                        sibling.left.color = BLACK;
                        sibling.color = RED;
                        rotateRight(sibling);
                        sibling = node.parent.right;
                    }
                    sibling.color = node.parent.color;
                    node.parent.color = BLACK;
                    sibling.right.color = BLACK;
                    rotateLeft(node.parent);
                    node = root;
                }
            } else {
                Node sibling = node.parent.left;
                if (sibling.color == RED) {
                    sibling.color = BLACK;
                    node.parent.color = RED;
                    rotateRight(node.parent);
                    sibling = node.parent.left;
                }
                if (sibling.right.color == BLACK && sibling.left.color == BLACK) {
                    sibling.color = RED;
                    node = node.parent;
                } else {
                    if (sibling.left.color == BLACK) {
                        sibling.right.color = BLACK;
                        sibling.color = RED;
                        rotateLeft(sibling);
                        sibling = node.parent.left;
                    }
                    sibling.color = node.parent.color;
                    node.parent.color = BLACK;
                    sibling.left.color = BLACK;
                    rotateRight(node.parent);
                    node = root;
                }
            }
        }
        node.color = BLACK;
    }

    private Node findNode(String key) {
        if (key == null) {
            return nil;
        }
        Node current = root;
        while (current != nil) {
            int comparison = key.compareTo(current.entry.getKey());
            if (comparison == 0) {
                return current;
            }
            current = comparison < 0 ? current.left : current.right;
        }
        return nil;
    }

    private Node minimum(Node node) {
        while (node.left != nil) {
            node = node.left;
        }
        return node;
    }

    private void transplant(Node oldNode, Node newNode) {
        if (oldNode.parent == nil) {
            root = newNode;
        } else if (oldNode == oldNode.parent.left) {
            oldNode.parent.left = newNode;
        } else {
            oldNode.parent.right = newNode;
        }
        newNode.parent = oldNode.parent;
    }
}
