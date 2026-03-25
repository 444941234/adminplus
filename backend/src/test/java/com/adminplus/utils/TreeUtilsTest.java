package com.adminplus.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TreeUtils 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
class TreeUtilsTest {

    /**
     * 测试用的树节点实现
     */
    static class TestNode implements TreeUtils.TreeNode<TestNode> {
        private String id;
        private String parentId;
        private List<TestNode> children;

        public TestNode(String id, String parentId) {
            this.id = id;
            this.parentId = parentId;
            this.children = new ArrayList<>();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getParentId() {
            return parentId;
        }

        @Override
        public List<TestNode> getChildren() {
            return children;
        }

        @Override
        public void setChildren(List<TestNode> children) {
            this.children = children;
        }
    }

    @Nested
    class BuildTreeTests {
        @Test
        void buildTree_WithEmptyList_ShouldReturnEmptyList() {
            // When
            List<TestNode> result = TreeUtils.buildTree(new ArrayList<TestNode>());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void buildTree_WithNullList_ShouldReturnEmptyList() {
            // When
            List<TestNode> result = TreeUtils.buildTree(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void buildTree_WithSingleNode_ShouldReturnSingleNode() {
            // Given
            List<TestNode> nodes = List.of(new TestNode("1", "0"));

            // When
            List<TestNode> result = TreeUtils.buildTree(nodes);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("1");
        }

        @Test
        void buildTree_WithParentChild_ShouldBuildCorrectTree() {
            // Given
            List<TestNode> nodes = List.of(
                    new TestNode("1", "0"),
                    new TestNode("2", "1"),
                    new TestNode("3", "1")
            );

            // When
            List<TestNode> result = TreeUtils.buildTree(nodes);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("1");
            assertThat(result.get(0).getChildren()).hasSize(2);
        }

        @Test
        void buildTree_WithMultipleLevels_ShouldBuildDeepTree() {
            // Given
            List<TestNode> nodes = List.of(
                    new TestNode("1", "0"),
                    new TestNode("2", "1"),
                    new TestNode("3", "2"),
                    new TestNode("4", "3")
            );

            // When
            List<TestNode> result = TreeUtils.buildTree(nodes);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("1");
            assertThat(result.get(0).getChildren()).hasSize(1);
            assertThat(result.get(0).getChildren().get(0).getId()).isEqualTo("2");
        }

        @Test
        void buildTree_WithMultipleRoots_ShouldReturnMultipleRoots() {
            // Given
            List<TestNode> nodes = List.of(
                    new TestNode("1", "0"),
                    new TestNode("2", "0"),
                    new TestNode("3", "1")
            );

            // When
            List<TestNode> result = TreeUtils.buildTree(nodes);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        void buildTree_WithNullParentId_ShouldTreatAsRoot() {
            // Given
            List<TestNode> nodes = List.of(
                    new TestNode("1", null),
                    new TestNode("2", "1")
            );

            // When
            List<TestNode> result = TreeUtils.buildTree(nodes);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("1");
        }
    }

    @Nested
    class FlattenTests {
        @Test
        void flatten_WithEmptyTree_ShouldReturnEmptyList() {
            // When
            List<TestNode> result = TreeUtils.flatten(new ArrayList<TestNode>());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void flatten_WithTree_ShouldReturnFlatList() {
            // Given
            TestNode root = new TestNode("1", "0");
            TestNode child1 = new TestNode("2", "1");
            TestNode child2 = new TestNode("3", "1");
            root.setChildren(List.of(child1, child2));

            // When
            List<TestNode> result = TreeUtils.flatten(List.of(root));

            // Then
            assertThat(result).hasSize(3);
        }

        @Test
        void flatten_WithDeepTree_ShouldReturnAllNodes() {
            // Given
            TestNode root = new TestNode("1", "0");
            TestNode child = new TestNode("2", "1");
            TestNode grandchild = new TestNode("3", "2");
            child.setChildren(List.of(grandchild));
            root.setChildren(List.of(child));

            // When
            List<TestNode> result = TreeUtils.flatten(List.of(root));

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.stream().map(TestNode::getId)).containsExactly("1", "2", "3");
        }
    }

    @Nested
    class GetDescendantIdsTests {
        @Test
        void getDescendantIds_WithLeafNode_ShouldReturnOnlySelf() {
            // Given
            TestNode root = new TestNode("1", "0");
            TestNode leaf = new TestNode("2", "1");
            root.setChildren(List.of(leaf));

            // When
            List<String> result = TreeUtils.getDescendantIds(List.of(root), "2");

            // Then
            assertThat(result).containsExactly("2");
        }

        @Test
        void getDescendantIds_WithParentNode_ShouldReturnAllDescendants() {
            // Given
            TestNode root = new TestNode("1", "0");
            TestNode child1 = new TestNode("2", "1");
            TestNode child2 = new TestNode("3", "1");
            root.setChildren(List.of(child1, child2));

            // When
            List<String> result = TreeUtils.getDescendantIds(List.of(root), "1");

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).contains("1", "2", "3");
        }
    }

    @Nested
    class FilterTreeTests {
        @Test
        void filterTree_WithNoMatches_ShouldReturnEmptyList() {
            // Given
            TestNode root = new TestNode("1", "0");
            TestNode child = new TestNode("2", "1");
            root.setChildren(List.of(child));

            // When
            List<TestNode> result = TreeUtils.filterTree(List.of(root), n -> false);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void filterTree_WithAllMatches_ShouldReturnFullTree() {
            // Given
            TestNode root = new TestNode("1", "0");
            TestNode child = new TestNode("2", "1");
            root.setChildren(List.of(child));

            // When
            List<TestNode> result = TreeUtils.filterTree(List.of(root), n -> true);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        void filterTree_WithChildMatch_ShouldKeepAncestorChain() {
            // Given
            TestNode root = new TestNode("1", "0");
            TestNode child = new TestNode("2", "1");
            root.setChildren(List.of(child));

            // When
            List<TestNode> result = TreeUtils.filterTree(List.of(root), n -> "2".equals(n.getId()));

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("1");
        }

        @Test
        void filterTree_WithEmptyTree_ShouldReturnEmptyList() {
            // When
            List<TestNode> result = TreeUtils.filterTree(new ArrayList<TestNode>(), n -> true);

            // Then
            assertThat(result).isEmpty();
        }
    }
}