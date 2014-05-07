/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.exsio.nestedj.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import pl.exsio.nestedj.FunctionalNestedjTest;
import pl.exsio.nestedj.model.TestNode;
import pl.exsio.nestedj.model.Tree;

/**
 *
 * @author exsio
 */
@Transactional
public class NestedNodeRepositoryTest extends FunctionalNestedjTest {

    @Autowired
    protected NestedNodeRepository<TestNode> nodeRepository;

    @PersistenceContext
    protected EntityManager em;
    
    /**
     * 
     *          STARTING NESTED TREE CONDITIONS
     * 
     *                      1 A 16
     *                       / \                    IDS:
     *                      /   \                   A: 1
     *                     /     \                  B: 2
     *                  2 B 7   8 C 15              C: 3
     *                   /         \                D: 4
     *                  /\         /\               E: 5
     *                 /  \       /  \              F: 6
     *                /    \     /    \             G: 7
     *               /   5 E 6  9 F 10 \            H: 8
     *             3 D 4             11 G 14
     *                                   \
     *                                    \
     *                                  12 H 13 
     */
    
    @Test
    public void testGetTree() {
        Tree<TestNode> tree = this.nodeRepository.getTree(this.findNode("a"));
        assertTrue(tree.getNode().getName().equals("a"));
        assertTrue(tree.getChildren().get(0).getNode().getName().equals("b"));
        assertTrue(tree.getChildren().size() == 2);
        assertTrue(tree.getChildren().get(0).getChildren().size() == 2);
        assertTrue(tree.getChildren().get(1).getChildren().size() == 2);
        assertTrue(tree.getChildren().get(1).getChildren().get(1).getChildren().size() == 1);
        assertTrue(tree.getChildren().get(1).getChildren().get(0).getChildren().isEmpty());
        assertTrue(tree.getChildren().get(0).getChildren().get(0).getChildren().isEmpty());
    }
    
    @Test
    public void testGetTreeAsList() {
        List<TestNode> list = (List<TestNode>) this.nodeRepository.getTreeAsList(this.findNode("a"));
        assertTrue(list.size() == 8);
    }
    
    @Test
    public void testGetParent() {
        TestNode b = this.findNode("b");
        TestNode parent = this.nodeRepository.getParent(b);
        assertTrue(parent instanceof TestNode);
        assertTrue(parent.getName().equals("a"));
    }
    
    @Test
    public void testRemoveSubtreeWithoutChildren() {
        
        TestNode d = this.findNode("d");
        this.nodeRepository.removeSubtree(d);
        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        TestNode b = this.findNode("b");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode h = this.findNode("h");
        TestNode f = this.findNode("f");
        
        assertTrue(e.getLeft() == 3);
        assertTrue(e.getRight() == 4);
        assertTrue(b.getRight() == 5);
        assertTrue(h.getLeft() == 10);
        assertTrue(h.getRight() == 11);
        assertTrue(a.getRight() == 14);
        assertTrue(c.getLeft() == 6);
        assertTrue(c.getRight() == 13);
        assertTrue(g.getLeft() == 9);
        assertTrue(g.getRight() == 12);
        
    }
    
    @Test
    public void testRemoveSubtree() {
        
        TestNode b = this.findNode("b");
        this.nodeRepository.removeSubtree(b);
        TestNode a = this.findNode("a");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode h = this.findNode("h");
        TestNode f = this.findNode("f");
        
        assertTrue(h.getLeft() == 6);
        assertTrue(h.getRight() == 7);
        assertTrue(a.getRight() == 10);
        assertTrue(c.getLeft() == 2);
        assertTrue(c.getRight() == 9);
        assertTrue(g.getLeft() == 5);
        assertTrue(g.getRight() == 8);
        
    }
    
    @Test
    public void testRemoveSingleNodeThatHasChildren() {
        
        TestNode b = this.findNode("b");
        this.nodeRepository.removeSingle(b);
        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode h = this.findNode("h");
        TestNode f = this.findNode("f");
        
        assertTrue(d.getLeft() == 2);
        assertTrue(d.getRight() == 3);
        assertTrue(e.getLeft() == 4);
        assertTrue(e.getRight() == 5);
        assertTrue(h.getLeft() == 10);
        assertTrue(h.getRight() == 11);
        assertTrue(a.getRight() == 14);
        assertTrue(c.getLeft() == 6);
        assertTrue(c.getRight() == 13);
        assertTrue(g.getLeft() == 9);
        assertTrue(g.getRight() == 12);
        
    }
    
    @Test
    public void testRemoveSingleNode() {
        
        TestNode d = this.findNode("d");
        this.nodeRepository.removeSingle(d);
        TestNode a = this.findNode("a");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode e = this.findNode("e");
        
        assertTrue(e.getLeft() == 3);
        assertTrue(e.getRight() == 4);
        assertTrue(a.getRight() == 14);
        assertTrue(c.getLeft() == 6);
        assertTrue(c.getRight() == 13);
        assertTrue(g.getLeft() == 9);
        assertTrue(g.getRight() == 12);
        
    }
    
    @Test
    public void testInsertAsLastChildOfDeepMove() {
        TestNode b = this.findNode("b");
        TestNode a = this.findNode("a");
        b = this.nodeRepository.insertAsLastChildOf(b, a);
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");

        assertTrue(c.getLeft() == 2);
        assertTrue(c.getRight() == 9);
        assertTrue(b.getLeft() == 10);
        assertTrue(b.getRight() == 15);
        assertTrue(g.getLeft() == 5);
        assertTrue(g.getRight() == 8);
        assertTrue(d.getLeft() == 11);
        assertTrue(d.getRight() == 12);
        assertTrue(b.getLevel() == 1);
        assertTrue(d.getLevel() == 2);
        assertTrue(this.getParent(b) == a);
    }

    @Test
    public void testInsertAsFirstChildOfDeepMove() {
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        c = this.nodeRepository.insertAsFirstChildOf(c, a);
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode b = this.findNode("b");

        assertTrue(c.getLeft() == 2);
        assertTrue(c.getRight() == 9);
        assertTrue(b.getLeft() == 10);
        assertTrue(b.getRight() == 15);
        assertTrue(g.getLeft() == 5);
        assertTrue(g.getRight() == 8);
        assertTrue(d.getLeft() == 11);
        assertTrue(d.getRight() == 12);
        assertTrue(g.getLevel() == 2);
        assertTrue(c.getLevel() == 1);
        assertTrue(this.getParent(c) == a);
    }

    @Test
    public void testInsertAsNextSiblingOfDeepMove() {
        TestNode b = this.findNode("b");
        TestNode a = this.findNode("a");
        b = this.nodeRepository.insertAsNextSiblingOf(b, a);
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode e = this.findNode("e");

        assertTrue(b.getLeft() == 11);
        assertTrue(b.getRight() == 16);
        assertTrue(a.getLeft() == 1);
        assertTrue(a.getRight() == 10);
        assertTrue(g.getLeft() == 5);
        assertTrue(g.getRight() == 8);
        assertTrue(d.getLeft() == 12);
        assertTrue(d.getRight() == 13);
        assertTrue(b.getLevel() == 0);
        assertTrue(d.getLevel() == 1);
        assertTrue(e.getLevel() == 1);
        assertTrue(this.getParent(b) == null);
    }

    @Test
    public void testInsertAsPrevSiblingOfDeepMove() {
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        c = this.nodeRepository.insertAsPrevSiblingOf(c, a);
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode f = this.findNode("f");
        TestNode h = this.findNode("h");

        assertTrue(c.getLeft() == 1);
        assertTrue(c.getRight() == 8);
        assertTrue(a.getLeft() == 9);
        assertTrue(a.getRight() == 16);
        assertTrue(g.getLeft() == 4);
        assertTrue(g.getRight() == 7);
        assertTrue(d.getLeft() == 11);
        assertTrue(d.getRight() == 12);
        assertTrue(c.getLevel() == 0);
        assertTrue(f.getLevel() == 1);
        assertTrue(g.getLevel() == 1);
        assertTrue(h.getLevel() == 2);
        assertTrue(this.getParent(c) == null);
    }

    @Test
    public void testInsertAsPrevSiblingOfMoveRight() {
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        d = this.nodeRepository.insertAsPrevSiblingOf(d, g);
        TestNode f = this.findNode("f");
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode e = this.findNode("e");
        TestNode h = this.findNode("h");

        assertTrue(e.getLeft() == 3);
        assertTrue(e.getRight() == 4);
        assertTrue(b.getRight() == 5);
        assertTrue(f.getLeft() == 7);
        assertTrue(f.getRight() == 8);
        assertTrue(g.getLeft() == 11);
        assertTrue(g.getRight() == 14);
        assertTrue(d.getLeft() == 9);
        assertTrue(d.getRight() == 10);
        assertTrue(h.getLeft() == 12);
        assertTrue(h.getRight() == 13);
        assertTrue(c.getRight() == 15);
        assertTrue(a.getRight() == 16);
        assertTrue(d.getLevel() == 2);

        assertTrue(this.getParent(d) == c);
    }

    @Test
    public void testInsertAsPrevSiblingOfMoveLeft() {
        TestNode g = this.findNode("g");
        TestNode e = this.findNode("e");
        g = this.nodeRepository.insertAsPrevSiblingOf(g, e);
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode h = this.findNode("h");

        assertTrue(g.getLeft() == 5);
        assertTrue(g.getRight() == 8);
        assertTrue(h.getLeft() == 6);
        assertTrue(h.getRight() == 7);
        assertTrue(e.getLeft() == 9);
        assertTrue(e.getRight() == 10);
        assertTrue(b.getRight() == 11);
        assertTrue(c.getLeft() == 12);
        assertTrue(c.getRight() == 15);
        assertTrue(a.getRight() == 16);
        assertTrue(g.getLevel() == 2);
        assertTrue(h.getLevel() == 3);
        assertTrue(this.getParent(g) == b);
    }

    @Test
    public void testInsertAsNextSiblingOfMoveRight() {
        TestNode d = this.findNode("d");
        TestNode f = this.findNode("f");
        d = this.nodeRepository.insertAsNextSiblingOf(d, f);
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode e = this.findNode("e");
        TestNode h = this.findNode("h");

        assertTrue(e.getLeft() == 3);
        assertTrue(e.getRight() == 4);
        assertTrue(b.getRight() == 5);
        assertTrue(f.getLeft() == 7);
        assertTrue(f.getRight() == 8);
        assertTrue(g.getLeft() == 11);
        assertTrue(g.getRight() == 14);
        assertTrue(d.getLeft() == 9);
        assertTrue(d.getRight() == 10);
        assertTrue(h.getLeft() == 12);
        assertTrue(h.getRight() == 13);
        assertTrue(c.getRight() == 15);
        assertTrue(a.getRight() == 16);
        assertTrue(d.getLevel() == 2);
        assertTrue(this.getParent(d) == c);
    }

    @Test
    public void testInsertAsNextSiblingOfMoveLeft() {
        TestNode g = this.findNode("g");
        TestNode d = this.findNode("d");
        g = this.nodeRepository.insertAsNextSiblingOf(g, d);
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode e = this.findNode("e");
        TestNode h = this.findNode("h");

        assertTrue(g.getLeft() == 5);
        assertTrue(g.getRight() == 8);
        assertTrue(h.getLeft() == 6);
        assertTrue(h.getRight() == 7);
        assertTrue(e.getLeft() == 9);
        assertTrue(e.getRight() == 10);
        assertTrue(b.getRight() == 11);
        assertTrue(c.getLeft() == 12);
        assertTrue(c.getRight() == 15);
        assertTrue(a.getRight() == 16);
        assertTrue(g.getLevel() == 2);
        assertTrue(h.getLevel() == 3);
        assertTrue(this.getParent(g) == b);
    }

    @Test
    public void testInsertAsLastChildOfMoveLeft() {
        TestNode g = this.findNode("g");
        TestNode b = this.findNode("b");
        g = this.nodeRepository.insertAsLastChildOf(g, b);
        TestNode f = this.findNode("f");
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode d = this.findNode("d");
        TestNode h = this.findNode("h");

        assertTrue(g.getLeft() == 7);
        assertTrue(g.getRight() == 10);
        assertTrue(h.getLeft() == 8);
        assertTrue(h.getRight() == 9);
        assertTrue(b.getRight() == 11);
        assertTrue(f.getLeft() == 13);
        assertTrue(f.getRight() == 14);
        assertTrue(c.getLeft() == 12);
        assertTrue(c.getRight() == 15);
        assertTrue(a.getRight() == 16);
        assertTrue(g.getLevel() == 2);
        assertTrue(h.getLevel() == 3);
        assertTrue(this.getParent(g) == b);
    }

    @Test
    public void testInsertAsLastChildOfMoveRight() {
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        d = this.nodeRepository.insertAsLastChildOf(d, g);
        TestNode f = this.findNode("f");
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode e = this.findNode("e");
        TestNode h = this.findNode("h");

        assertTrue(e.getLeft() == 3);
        assertTrue(e.getRight() == 4);
        assertTrue(b.getRight() == 5);
        assertTrue(f.getLeft() == 7);
        assertTrue(g.getLeft() == 9);
        assertTrue(d.getLeft() == 12);
        assertTrue(d.getRight() == 13);
        assertTrue(h.getLeft() == 10);
        assertTrue(h.getRight() == 11);
        assertTrue(c.getRight() == 15);
        assertTrue(a.getRight() == 16);
        assertTrue(d.getLevel() == 3);
        assertTrue(this.getParent(d) == g);
    }

    @Test
    public void testInsertAsFirstChildOfMoveRight() {
        TestNode d = findNode("d");
        TestNode g = findNode("g");
        d = this.nodeRepository.insertAsFirstChildOf(d, g);
        TestNode f = findNode("f");
        TestNode c = findNode("c");
        TestNode a = findNode("a");
        TestNode b = findNode("b");
        TestNode e = findNode("e");
        TestNode h = findNode("h");

        assertTrue(e.getLeft() == 3);
        assertTrue(e.getRight() == 4);
        assertTrue(b.getRight() == 5);
        assertTrue(f.getLeft() == 7);
        assertTrue(g.getLeft() == 9);
        assertTrue(d.getLeft() == 10);
        assertTrue(d.getRight() == 11);
        assertTrue(h.getLeft() == 12);
        assertTrue(h.getRight() == 13);
        assertTrue(c.getRight() == 15);
        assertTrue(a.getRight() == 16);
        assertTrue(d.getLevel() == 3);
        assertTrue(this.getParent(d) == g);
    }

    @Test
    public void testInsertAsFirstChildOfMoveLeft() {
        TestNode g = this.findNode("g");
        TestNode b = this.findNode("b");
        g = this.nodeRepository.insertAsFirstChildOf(g, b);
        TestNode f = this.findNode("f");
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode h = this.findNode("h");

        assertTrue(g.getLeft() == 3);
        assertTrue(g.getRight() == 6);
        assertTrue(f.getLeft() == 13);
        assertTrue(f.getRight() == 14);
        assertTrue(c.getLeft() == 12);
        assertTrue(c.getRight() == 15);
        assertTrue(a.getRight() == 16);
        assertTrue(g.getLevel() == 2);
        assertTrue(h.getLevel() == 3);
        assertTrue(this.getParent(g) == b);
    }


    @Test
    public void testGetChildren() {

        List result = (List) this.nodeRepository.getChildren(this.findNode("a"));
        assertTrue(result.size() == 2);
    }

    @Test
    public void testInsertAsFirstChildOfInsert() {

        TestNode i = this.getTestNode("i");
        TestNode e = this.findNode("e");
        i = this.nodeRepository.insertAsFirstChildOf(i, e);
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode h = this.findNode("h");

        assertTrue(i.getLeft() == 6);
        assertTrue(i.getRight() == 7);
        assertTrue(a.getRight() == 18);
        assertTrue(b.getRight() == 9);
        assertTrue(h.getLeft() == 14);
        assertTrue(h.getRight() == 15);
    }

    @Test
    public void testInsertAsLastChildOfInsert() {

        TestNode j = this.getTestNode("j");
        TestNode b = this.findNode("b");
        j = this.nodeRepository.insertAsLastChildOf(j, b);
        TestNode a = this.findNode("a");
        TestNode h = this.findNode("h");
        TestNode c = this.findNode("c");

        assertTrue(j.getLeft() == 7);
        assertTrue(j.getRight() == 8);
        assertTrue(a.getRight() == 18);
        assertTrue(h.getLeft() == 14);
        assertTrue(h.getRight() == 15);
        assertTrue(c.getLeft() == 10);
    }

    @Test
    public void testInsertAsPrevSiblingOfInsert() {

        TestNode k = this.getTestNode("k");
        TestNode e = this.findNode("e");
        k = this.nodeRepository.insertAsPrevSiblingOf(k, e);
        TestNode a = this.findNode("a");
        TestNode h = this.findNode("h");
        TestNode c = this.findNode("c");

        assertTrue(k.getLeft() == 5);
        assertTrue(k.getRight() == 6);
        assertTrue(a.getRight() == 18);
        assertTrue(h.getLeft() == 14);
        assertTrue(h.getRight() == 15);
        assertTrue(c.getLeft() == 10);
    }

    @Test
    public void testInsertAsNextSiblingOfInsert() {

        TestNode m = this.getTestNode("m");
        TestNode h = this.findNode("h");
        m = this.nodeRepository.insertAsNextSiblingOf(m, h);
        TestNode a = this.findNode("a");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");

        assertTrue(m.getLeft() == 14);
        assertTrue(m.getRight() == 15);
        assertTrue(a.getRight() == 18);
        assertTrue(g.getRight() == 16);
        assertTrue(c.getRight() == 17);
    }
    
    

    private TestNode getTestNode(String symbol) {

        TestNode n = new TestNode();
        n.setName(symbol);
        return n;
    }

    private TestNode getParent(TestNode f) {
        this.em.refresh(f);
        TestNode parent = this.nodeRepository.getParent(f);
        if (parent instanceof TestNode) {
            this.em.refresh(parent);
        }
        return parent;
    }

    private TestNode findNode(String symbol) {

        Map<String, Integer> nodeMap = new HashMap() {
            {
                put("a", 1);
                put("b", 2);
                put("c", 3);
                put("d", 4);
                put("e", 5);
                put("f", 6);
                put("g", 7);
                put("h", 8);
            }
        };

        TestNode n = this.em.find(TestNode.class,nodeMap.get(symbol));
        this.em.refresh(n);
        return n;
    }

}