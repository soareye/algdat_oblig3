import sun.awt.image.ImageWatched;

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

                current = current.høyre;
                while (current != null) {
                    deque.addFirst(current);
                    current = current.høyre;
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

        while (node != null) {
            int cmp = comp.compare(verdi, node.verdi);
            if (cmp < 0) node = node.venstre;
            else if (cmp > 0) node = node.høyre;
            else break;
        }

        if (node == null) return false;

        if (node.venstre == null || node.høyre == null) {
            Node<T> child = node.venstre != null ? node.venstre : node.høyre;

            if (node == rot) rot = child;
            else if (node == node.forelder.venstre) node.forelder.venstre = child;
            else node.forelder.høyre = child;

            if (child != null) child.forelder = node.forelder;

        } else {
            Node<T> parent = node;
            Node<T> rightChild = node.høyre;

            while (rightChild.venstre != null) {
                parent = rightChild;
                rightChild = rightChild.venstre;
            }

            node.verdi = rightChild.verdi;

            if (parent != node) parent.venstre = rightChild.høyre;
            else parent.høyre = rightChild.høyre;

            if (rightChild.høyre != null) rightChild.høyre.forelder = parent;
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

        StringBuilder strBuilder = new StringBuilder(node.verdi.toString());

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
    public String[] branches() {
        String branchList[] = new String[countLeafs(rot)];

        ArrayDeque<Node<T>> deque = new ArrayDeque<>();

        if (rot != null) {
            deque.addLast(rot);
            branchList[0] = rot.verdi.toString();
        }

        while (deque.size() > 0) {
            for (int i = 0; i < deque.size(); i++) {
                Node<T> current = deque.removeFirst();

            }
        }

        return branchList;
    }

    // Oppgave 7
    public String[] grener() {
        String branchList[] = new String[countLeafs(rot)];
        createBranch(rot, branchList, "[");
        return branchList;
    }

    // Oppgave 7 hjelpemetode
    private void createBranch(Node<T> node, String list[], String branch) {
        if (node == null) return;

        branch += node.verdi;

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

        while (node.venstre != null || node.høyre != null) {
            if (node.venstre != null) node = node.venstre;
            else node = node.høyre;
        }

        for (int i = 0; i < antall; i++) {
            stringBuilder.append(node.verdi);

            if (node.forelder != null) {
                if (node == node.forelder.høyre || node.forelder.høyre == null) {
                    node = node.forelder;

                } else {
                    node = node.forelder.høyre;
                    while (node.venstre != null || node.høyre != null) {
                        if (node.venstre != null) node = node.venstre;
                        else node = node.høyre;
                    }
                }

                stringBuilder.append(", ");
            }
        }

        stringBuilder.append("]");
        return stringBuilder.toString();
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

            while (p.forelder != null &&
                    (p == p.forelder.høyre || p.forelder.høyre == null)) {

                p = p.forelder;
            }

            if (p.forelder != null) {
                p = p.forelder.høyre;

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