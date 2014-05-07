/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.exsio.nestedj.mover;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.NestedNodeUtil;
import pl.exsio.nestedj.NestedNodeMover;
import pl.exsio.nestedj.config.NestedNodeConfig;

/**
 *
 * @author exsio
 */
public class JpaNestedNodeMover implements NestedNodeMover {

    private final static String SIGN_PLUS = "+";
    private final static String SIGN_MINUS = "-";

    @PersistenceContext
    protected EntityManager em;

    /**
     *
     */
    protected NestedNodeUtil util;

    /**
     * 
     */
    public JpaNestedNodeMover() {
    }

    /**
     * 
     * @param em 
     */
    public JpaNestedNodeMover(EntityManager em) {
        this.em = em;
    }

    /**
     *
     * @param util
     */
    public void setNestedNodeUtil(NestedNodeUtil util) {
        this.util = util;
    }

    @Override
    @Transactional
    public NestedNode move(NestedNode node, NestedNode parent, int mode) {

        NestedNodeConfig config = this.util.getNodeConfig(node.getClass());
        String sign = this.getSign(node, parent, mode);
        Integer start = this.getStart(node, parent, mode, sign);
        Integer stop = this.getStop(node, parent, mode, sign);
        List nodeIds = this.getNodeIds(node, config);
        Integer delta = this.getDelta(nodeIds);
        Integer nodeDelta = this.getNodeDelta(start, stop);
        String nodeSign = this.getNodeSign(sign);
        Integer levelModificator = this.getLevelModificator(node, parent, mode);
        NestedNode newParent = this.getNewParent(parent, mode);

        this.em.createQuery("update " + config.getEntityName() + " set " + config.getLeftFieldName() + " = " + config.getLeftFieldName() + " " + sign + ":delta where " + config.getLeftFieldName() + " > :start and " + config.getLeftFieldName() + " < :stop").setParameter("delta", delta).setParameter("start", start).setParameter("stop", stop).executeUpdate();
        this.em.createQuery("update " + config.getEntityName() + " set " + config.getRightFieldName() + " = " + config.getRightFieldName() + " " + sign + ":delta where " + config.getRightFieldName() + " > :start and " + config.getRightFieldName() + " < :stop").setParameter("delta", delta).setParameter("start", start).setParameter("stop", stop).executeUpdate();
        this.em.createQuery("update " + config.getEntityName() + " set " + config.getLevelFieldName() + " = " + config.getLevelFieldName() + " + :levelModificator, " + config.getRightFieldName() + " = " + config.getRightFieldName() + " " + nodeSign + ":nodeDelta, " + config.getLeftFieldName() + " = " + config.getLeftFieldName() + " " + nodeSign + ":nodeDelta where id in :ids").setParameter("nodeDelta", nodeDelta).setParameter("ids", nodeIds).setParameter("levelModificator", levelModificator).executeUpdate();
        this.em.createQuery("update " + config.getEntityName() + " set " + config.getParentFieldName() + " = :parent where id = :id").setParameter("parent", newParent).setParameter("id", node.getId()).executeUpdate();
        this.em.refresh(parent);
        this.em.refresh(node);

        return node;
    }

    private NestedNode getNewParent(NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_PREV_SIBLING:
                return parent.getParent();
            case MODE_FIRST_CHILD:
            case MODE_LAST_CHILD:
            default:
                return parent;
        }
    }

    private Integer getLevelModificator(NestedNode node, NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_PREV_SIBLING:
                return parent.getLevel() - node.getLevel();
            case MODE_FIRST_CHILD:
            case MODE_LAST_CHILD:
            default:
                return parent.getLevel() + 1 - node.getLevel();
        }
    }

    private List<Long> getNodeIds(NestedNode node, NestedNodeConfig config) {
        List result = this.em.createQuery("select id from " + config.getEntityName() + " where " + config.getLeftFieldName() + ">=:lft and " + config.getRightFieldName() + " <=:rgt ").setParameter("lft", node.getLeft()).setParameter("rgt", node.getRight()).getResultList();
        return result;
    }

    private Integer getNodeDelta(Integer start, Integer stop) {
        return stop - start - 1;
    }

    private Integer getDelta(List<Long> nodeIds) {
        return nodeIds.size() * 2;
    }

    private String getNodeSign(String sign) {
        return (sign.equals(SIGN_PLUS)) ? SIGN_MINUS : SIGN_PLUS;
    }

    private String getSign(NestedNode node, NestedNode parent, int mode) {
        switch (mode) {
            case MODE_PREV_SIBLING:
            case MODE_FIRST_CHILD:
                return (node.getRight() - parent.getLeft()) > 0 ? SIGN_PLUS : SIGN_MINUS;
            case MODE_NEXT_SIBLING:
            case MODE_LAST_CHILD:
            default:
                return (node.getLeft() - parent.getRight()) > 0 ? SIGN_PLUS : SIGN_MINUS;
        }
    }

    private Integer getStart(NestedNode node, NestedNode parent, int mode, String sign) {
        switch (mode) {
            case MODE_PREV_SIBLING:
                return sign.equals(SIGN_PLUS) ? parent.getLeft() - 1 : node.getRight();
            case MODE_FIRST_CHILD:
                return sign.equals(SIGN_PLUS) ? parent.getLeft() : node.getRight();
            case MODE_NEXT_SIBLING:
                return sign.equals(SIGN_PLUS) ? parent.getRight() : node.getRight();
            case MODE_LAST_CHILD:
            default:
                return sign.equals(SIGN_PLUS) ? parent.getRight() - 1 : node.getRight();

        }
    }

    private Integer getStop(NestedNode node, NestedNode parent, int mode, String sign) {
        switch (mode) {
            case MODE_PREV_SIBLING:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getLeft();
            case MODE_FIRST_CHILD:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getLeft() + 1;
            case MODE_NEXT_SIBLING:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getRight() + 1;
            case MODE_LAST_CHILD:
            default:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getRight();
        }
    }

}