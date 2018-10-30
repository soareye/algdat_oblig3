// Erik Snilsberg s325872

import java.util.*;

public class ObligSBinTre<T> implements Beholder<T> {

    private static final class Node<T> {

        private T verdi;
        private Node<T> venstre, høyre;
        private Node<T> forelder;

        private Node(T verdi, Node<T> v, Node<T> h, Node<T> forelder) {
            this.verdi = verdi;
            venstre = v; høyre = h;
            this.forelder = forelder;
        }

        private Node(T verdi, Node<T> forelder) {
            this(verdi, null, null, forelder);
        }

        @Override
        public String toString(){ return "" + verdi;}

    }

    private Node<T> rot;
    private int antall;
    private int endringer;
    private final Comparator<? super T> comp;

    public ObligSBinTre(Comparator<? super T> c) {
        rot = null;
        antall = 0;
        comp = c;
    }

    // Oppgave 1
    @Override
    public final boolean leggInn(T verdi) {
        Objects.requireNonNull(verdi, "Ulovlig med nullverdier!");

        Node<T> p = rot, q = null;
        int cmp = 0;

        while (p != null) {
            q = p;
            cmp = comp.compare(verdi, p.verdi);
            p = cmp < 0 ? p.venstre : p.høyre;
        }

        p = new Node<>(verdi, q);

        if (q == null) rot = p;
        else if (cmp < 0) q.venstre = p;
        else q.høyre = p;

        endringer++;
        antall++;
        return true;
    }

    @Override
    public boolean inneholder(T verdi) {
        if (verdi == null) return false;

        Node<T> p = rot;
        while (p != null) {
            int cmp = comp.compare(verdi, p.verdi);
            if (cmp < 0) p = p.venstre;
            else if (cmp > 0) p = p.høyre;
            else return true;
        }

        return false;
    }

    @Override
    public int antall() {
        return antall;
    }

    // Oppgave 2
    public int antall(T verdi) {
        return antallRec(verdi, rot);
    }

    // Oppgave 2 hjelpemetode
    private int antallRec(T verdi, Node<T> node) {

        int result = 0;

        if (node == null) return result;

        if (node.verdi.equals(verdi)) {
            result = 1;

        } else if (node.venstre != null) {
            result += antallRec(verdi, node.venstre);
        }

        if (node.høyre != null) {
            result += antallRec(verdi, node.høyre);
        }

        return result;
    }

    @Override
    public boolean tom() {
        return antall == 0;
    }

    // Oppgave 3
    private static <T> Node<T> nesteInorden(Node<T> node) {

        if (node.høyre != null) {
            node = node.høyre;

            while (node.venstre != null) {
                node = node.venstre;
            }

            return node;
        }

        while (node.forelder != null && node == node.forelder.høyre) {
            node = node.forelder;
        }

        return node.forelder;
    }

    // Oppgave 3
    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder("[");

        Node<T> node = rot;

        // Find first node inorder:
        if (node != null) while (node.venstre != null) node = node.venstre;

        while (node != null) {
            strBuilder.append(node.verdi);

            Node<T> neste = nesteInorden(node);
            if (neste != null) strBuilder.append(", ");

            node = neste;
        }

        strBuilder.append("]");
        return strBuilder.toString();
    }

    // Oppgave 4
    public String omvendtString() {
        StringBuilder strBuilder = new StringBuilder("[");

        ArrayDeque<Node<T>> deque = new ArrayDeque<>();
        Node<T> node = rot;

        while (node != null) {
            deque.addFirst(node);
            node = node.høyre;
        }

        while (deque.size() > 0) {
            Node<T> current = deque.removeFirst();
            strBuilder.append(current.verdi);

            if (current.venstre != null) {
                current = current.venstre;
                deque.addFirst(current);

                while (current.høyre != null) {
                    current = current.høyre;
                    deque.addFirst(current);
                }
            }

            if (deque.size() > 0)
                strBuilder.append(", ");
        }

        strBuilder.append("]");
        return strBuilder.toString();
    }

    // Oppgave 5
    @Override
    public boolean fjern(T verdi) {
        if (verdi == null) return false;

        Node<T> node = rot;

        // Find the node to remove by traversing the tree,
        // comparing the 'verdi'-attributes:
        while (node != null) {
            int cmp = comp.compare(verdi, node.verdi);
            if (cmp < 0) node = node.venstre;
            else if (cmp > 0) node = node.høyre;
            else break;
        }

        // If node equals null, then the value isn't in the tree:
        if (node == null) return false;

        if (node.venstre == null || node.høyre == null) {
            // If one of the children doesn't equal null, find that child:
            Node<T> child = node.venstre != null ? node.venstre : node.høyre;

            // If we're removing the root node, set root to child.
            // Else place the child in the node's parent left or right pointer:
            if (node == rot) rot = child;
            else if (node == node.forelder.venstre) node.forelder.venstre = child;
            else node.forelder.høyre = child;

            if (child != null) child.forelder = node.forelder;

        } else {
            // The leftmost child of the node's right child is
            // the value that can replace the one we are removing:
            Node<T> parent = node;
            Node<T> child = node.høyre;
            while (child.venstre != null) {
                parent = child;
                child = child.venstre;
            }

            node.verdi = child.verdi;

            // We have moved "child"'s value to the node we're removing.
            // "child" doesn't have a left child so we place it's right child
            // in "parent"'s right child or left child depending on how far we moved:
            if (parent != node) parent.venstre = child.høyre;
            else parent.høyre = child.høyre;

            // Finally, we update the parent-reference of "child"'s right child if it's not null:
            if (child.høyre != null) child.høyre.forelder = parent;
        }

        endringer++;
        antall--;
        return true;
    }

    // Oppgave 5
    public int fjernAlle(T verdi) {
        int occurrences = antall(verdi);

        for (int i = 0; i < occurrences; i++) {
            fjern(verdi);
        }

        return occurrences;
    }

    // Oppgave 5
    @Override
    public void nullstill() {
        nullstillSubtre(rot);
        rot = null;
    }

    // Oppgave 5 hjelpemetode
    private void nullstillSubtre(Node<T> node) {
        if (node == null) return;

        if (node.venstre != null) {
            nullstillSubtre(node.venstre);
            node.venstre = null;
        }

        if (node.høyre != null) {
            nullstillSubtre(node.høyre);
            node.høyre = null;
        }

        node.forelder = null;
        node.verdi = null;
        endringer++;
        antall--;
    }

    // Oppgave 6
    public String høyreGren() {
        return "[" + høyreGrenSubtre(rot) + "]";
    }

    // Oppgave 6 hjelpemetode
    private String høyreGrenSubtre(Node<T> node) {
        if (node == null) return "";

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(node.verdi);

        if (node.høyre != null) {
            strBuilder.append(", ");
            strBuilder.append(høyreGrenSubtre(node.høyre));

        } else if (node.venstre != null) {
            strBuilder.append(", ");
            strBuilder.append(høyreGrenSubtre(node.venstre));
        }

        return strBuilder.toString();
    }

    // Oppgave 6
    public String lengstGren() {
        return "[" + longestBranchSubtree(rot) + "]";
    }

    // Oppgave 6 hjelpemetode
    private String longestBranchSubtree(Node<T> node) {
        if (node == null) return "";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(node.verdi);

        int leftHeight = heightSubtree(node.venstre);
        int rightHeight = heightSubtree(node.høyre);

        String left = longestBranchSubtree(node.venstre);
        String right = longestBranchSubtree(node.høyre);

        if (leftHeight > 0 || rightHeight > 0)
            stringBuilder.append(", ");

        // Add the longest branch to the stringbuilder:
        stringBuilder.append(rightHeight > leftHeight ? right : left);

        return stringBuilder.toString();
    }

    // Oppgave 6 hjelpemetode
    private int heightSubtree(Node<T> node) {
        if (node == null) {
            return 0;

        } else {
            return 1 + Math.max(heightSubtree(node.venstre), heightSubtree(node.høyre));
        }
    }

    // Oppgave 7
    public String[] grener() {
        // Create an array of the correct length:
        String branchList[] = new String[countLeafs(rot)];
        // Create all branches from the root node:
        createBranch(rot, branchList, "[");
        return branchList;
    }

    // Oppgave 7 hjelpemetode
    private void createBranch(Node<T> node, String list[], String branch) {
        if (node == null) return;

        branch += node.verdi;

        // When the method reaches a leaf, add the current branch-string to the branch-list:
        if (node.venstre == null && node.høyre == null) {
            branch += "]";
            addBranch(branch, list);

        } else {
            branch += ", ";
        }

        createBranch(node.venstre, list, branch);
        createBranch(node.høyre, list, branch);
    }

    // Oppgave 7 hjelpemetode
    // Add a string to the first index that's not null in a list:
    private void addBranch(String branch, String branchList[]) {
        int i = 0;
        while (i < branchList.length - 1 && branchList[i] != null)
            i++;

        branchList[i] = branch;
    }

    // Oppgave 7 hjelpemetode
    private int countLeafs(Node<T> node) {
        if (node == null) {
            return 0;

        } else if (node.venstre == null && node.høyre == null) {
            return 1;

        } else {
            return countLeafs(node.høyre) + countLeafs(node.venstre);
        }
    }

    // Oppgave 8 a)
    public String bladnodeverdier() {
        if (rot == null) return "[]";

        StringBuilder stringBuilder = new StringBuilder("[");

        ArrayDeque<Node<T>> deque = new ArrayDeque<>();
        deque.addFirst(rot);

        while (deque.size() > 0) {
            Node<T> node = deque.removeFirst();

            if (node.høyre != null)
                deque.addFirst(node.høyre);

            if (node.venstre != null)
                deque.addFirst(node.venstre);

            // Only add the value if the current node is a leaf:
            if (node.venstre == null && node.høyre == null) {
                stringBuilder.append(node.verdi);

                if (deque.size() > 0)
                    stringBuilder.append(", ");
            }
        }

        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    // Oppgave 8 b)
    public String postString() {
        if (rot == null) return "[]";

        StringBuilder stringBuilder = new StringBuilder("[");

        Node<T> node = rot;

        // Find the leftmost leaf-node:
        while (node.venstre != null || node.høyre != null) {
            if (node.venstre != null) node = node.venstre;
            else node = node.høyre;
        }

        // Do a for-loop because we know we have to go through every single node:
        for (int i = 0; i < antall; i++) {
            stringBuilder.append(node.verdi);

            if (node.forelder != null) {
                node = nextNodePostorder(node);
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append("]");
        return stringBuilder.toString();
    }


    // If current node is the rightmost child, then next node is the parent.
    // Else next node is the leftmost leaf-node of the current node's sibling:
    private Node<T> nextNodePostorder(Node<T> node) {
        if (node == node.forelder.høyre || node.forelder.høyre == null) {
            node = node.forelder;

        } else {
            node = node.forelder.høyre;
            while (node.venstre != null || node.høyre != null) {
                if (node.venstre != null) node = node.venstre;
                else node = node.høyre;
            }
        }

        return node;
    }

    @Override
    public Iterator<T> iterator() {
        return new BladnodeIterator();
    }

    private class BladnodeIterator implements Iterator<T> {

        private Node<T> p = rot, q = null;
        private boolean removeOK = false;
        private int iteratorendringer = endringer;

        // Oppgave 9
        private BladnodeIterator() {
            if (p != null) {
                while (p.venstre != null || p.høyre != null) {
                    if (p.venstre != null) p = p.venstre;
                    else p = p.høyre;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return p != null;
        }

        // Oppgave 9
        @Override
        public T next() {
            if (iteratorendringer != endringer)
                throw new ConcurrentModificationException();

            if (p == null)
                throw new NoSuchElementException();

            T r = p.verdi;
            q = p;

            // Move up as long as 'p' doesn't have a right sibling:
            while (p.forelder != null &&
                    (p == p.forelder.høyre || p.forelder.høyre == null)) {

                p = p.forelder;
            }

            if (p.forelder != null) {
                p = p.forelder.høyre;

                // Find the leftmost leaf-node:
                while (p.venstre != null || p.høyre != null) {
                    if (p.venstre != null) p = p.venstre;
                    else p = p.høyre;
                }

            } else {
                p = null;
            }

            removeOK = true;
            return r;
        }

        // Oppgave 10
        @Override
        public void remove() {
            if (!removeOK)
                throw new IllegalStateException();

            if (iteratorendringer != endringer)
                throw new ConcurrentModificationException();

            Node<T> parent = q.forelder;
            if (parent != null) {
                if (q == parent.høyre) parent.høyre = null;
                else parent.venstre = null;

            } else {
                rot = null;
            }

            q.forelder = null;
            q.verdi = null;
            q = null;

            antall--;
            iteratorendringer++;
            endringer++;
            removeOK = false;
        }
    }
}