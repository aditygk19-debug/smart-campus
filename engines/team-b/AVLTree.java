public class AVLTree {

    private class Node {

        RoutingEntry entry;
        Node left;
        Node right;
        int height;

        Node(RoutingEntry entry) {
            this.entry = entry;
            this.height = 1;
        }
    }

    private Node root;

    // =====================================================
    // HEIGHT
    // =====================================================

    private int height(Node node) {

        if (node == null) {
            return 0;
        }

        return node.height;
    }

    // =====================================================
    // MAX
    // =====================================================

    private int max(int a, int b) {

        return (a > b) ? a : b;
    }

    // =====================================================
    // RIGHT ROTATION
    // =====================================================

    private Node rightRotate(Node y) {

        Node x = y.left;
        Node temp = x.right;

        x.right = y;
        y.left = temp;

        y.height = 1 + max(
                height(y.left),
                height(y.right));

        x.height = 1 + max(
                height(x.left),
                height(x.right));

        return x;
    }

    // =====================================================
    // LEFT ROTATION
    // =====================================================

    private Node leftRotate(Node x) {

        Node y = x.right;
        Node temp = y.left;

        y.left = x;
        x.right = temp;

        x.height = 1 + max(
                height(x.left),
                height(x.right));

        y.height = 1 + max(
                height(y.left),
                height(y.right));

        return y;
    }

    // =====================================================
    // BALANCE FACTOR
    // =====================================================

    private int getBalance(Node node) {

        if (node == null) {
            return 0;
        }

        return height(node.left)
                - height(node.right);
    }

    // =====================================================
    // INSERT
    // =====================================================

    public void insert(RoutingEntry entry) {

        root = insertNode(root, entry);
    }

    private Node insertNode(
            Node node,
            RoutingEntry entry) {

        if (node == null) {

            return new Node(entry);
        }

        int comparison = entry.getKey()
                .compareTo(node.entry.getKey());

        if (comparison < 0) {

            node.left = insertNode(
                    node.left,
                    entry);

        } else if (comparison > 0) {

            node.right = insertNode(
                    node.right,
                    entry);

        } else {

            node.entry = entry;

            return node;
        }

        node.height = 1 + max(
                height(node.left),
                height(node.right));

        int balance = getBalance(node);

        // =================================================
        // LEFT LEFT
        // =================================================

        if (balance > 1
                && entry.getKey()
                        .compareTo(
                                node.left.entry.getKey()) < 0) {

            return rightRotate(node);
        }

        // =================================================
        // RIGHT RIGHT
        // =================================================

        if (balance < -1
                && entry.getKey()
                        .compareTo(
                                node.right.entry.getKey()) > 0) {

            return leftRotate(node);
        }

        // =================================================
        // LEFT RIGHT
        // =================================================

        if (balance > 1
                && entry.getKey()
                        .compareTo(
                                node.left.entry.getKey()) > 0) {

            node.left = leftRotate(node.left);

            return rightRotate(node);
        }

        // =================================================
        // RIGHT LEFT
        // =================================================

        if (balance < -1
                && entry.getKey()
                        .compareTo(
                                node.right.entry.getKey()) < 0) {

            node.right = rightRotate(node.right);

            return leftRotate(node);
        }

        return node;
    }

    // =====================================================
    // NORMAL SEARCH
    // =====================================================

    public RoutingEntry search(String key) {

        Node current = root;

        while (current != null) {

            int comparison = key.compareTo(
                    current.entry.getKey());

            if (comparison == 0) {

                return current.entry;
            }

            if (comparison < 0) {

                current = current.left;

            } else {

                current = current.right;
            }
        }

        return null;
    }



    // =====================================================
    // REMOVE / INVALIDATE
    // =====================================================

    public void remove(String key) {
        root = deleteNode(root, key);
    }

    private Node deleteNode(Node node, String key) {

        if (node == null) {
            return null;
        }

        int comparison = key.compareTo(node.entry.getKey());

        if (comparison < 0) {

            node.left = deleteNode(node.left, key);

        } else if (comparison > 0) {

            node.right = deleteNode(node.right, key);

        } else {

            if (node.left == null || node.right == null) {

                Node replacement =
                        (node.left != null)
                                ? node.left
                                : node.right;

                if (replacement == null) {
                    return null;
                }

                node = replacement;

            } else {

                Node successor = minValueNode(node.right);

                node.entry = successor.entry;

                node.right = deleteNode(
                        node.right,
                        successor.entry.getKey());
            }
        }

        node.height = 1 + max(
                height(node.left),
                height(node.right));

        int balance = getBalance(node);

        if (balance > 1
                && getBalance(node.left) >= 0) {

            return rightRotate(node);
        }

        if (balance > 1
                && getBalance(node.left) < 0) {

            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        if (balance < -1
                && getBalance(node.right) <= 0) {

            return leftRotate(node);
        }

        if (balance < -1
                && getBalance(node.right) > 0) {

            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    private Node minValueNode(Node node) {

        Node current = node;

        while (current.left != null) {
            current = current.left;
        }

        return current;
    }

    // =====================================================
    // VERSION-AWARE SEARCH
    // =====================================================

    public RoutingEntry search(
            String key,
            String currentTopologyVersion) {

        RoutingEntry entry = search(key);

        if (entry == null) {

            return null;
        }

        // Check expiry
        if (entry.isExpired()) {

            System.out.println(
                    "Cache entry expired for key: "
                            + key);

            return null;
        }

        // Check topology version
        if (!entry.getTopologyVersion()
                .equals(currentTopologyVersion)) {

            System.out.println(
                    "Topology version mismatch for key: "
                            + key);

            System.out.println(
                    "Cached Version: "
                            + entry.getTopologyVersion());

            System.out.println(
                    "Current Version: "
                            + currentTopologyVersion);

            return null;
        }

        return entry;
    }
}