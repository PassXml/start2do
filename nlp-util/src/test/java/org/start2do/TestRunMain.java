package org.start2do;

import java.io.IOException;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class TestRunMain {

    private static final int VECTOR_DIMENSION = 3;

    // 创建示例向量
    private static float[] createVector(float... values) {
        return values;
    }

    // 向索引添加文档
    private static void addDocument(IndexWriter writer, String id, String text) throws IOException {
        Document doc = new Document();
        doc.add(new StoredField("id", id));
//        doc.add(new StoredField("text", text));
        doc.add(new TextField("text", text, Store.YES));
//        doc.add(new KnnFloatVectorField("vector", t));  // 向量字段
        writer.addDocument(doc);
    }

//    private static SynonymMap synonymMap() {
//        SynonymMap.Builder synonymMapBuilder = new SynonymMap.Builder();
//        synonymMapBuilder.add(new String[]{"手机"}, new String[]{"移动电话"}, true);
//        synonymMapBuilder.add(new String[]{"汽车"}, new String[]{"小汽车", "轿车"}, true);
//        SynonymMap synonymMap = synonymMapBuilder.build();
//        return synonymMap;
//    }


    public static void main(String[] args) throws IOException {
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer() ;
        Directory index = new ByteBuffersDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(index, config)) {
            // 添加一些文档和向量
            addDocument(writer, "doc1", "站点发生了乘客受伤事件");
            String text = """
                1 行调了解火灾情况，根据火灾区域及影响范围，采取越站、扣车
                或小交路等方式调整行车，最大限度维持运营。
                2 环调跟进现场火势情况，根据火灾区域确认火灾模式是否启动，
                相关联动设备是否动作到位。
                3 电调加强供电设备监控，视情况组织接触网停电。
                4 设调跟进信息发布，协助值班主任电话通报相关领导或部门。""";
            addDocument(writer, "doc2", text);
            addDocument(writer, "doc3", "天气预报说今天会下雨");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (DirectoryReader reader = DirectoryReader.open(index)) {
            System.out.println("Documents in index: " + reader.numDocs());
            IndexSearcher searcher = new IndexSearcher(reader);
//            searcher.setSimilarity(new BM25Similarity());
//            KnnFloatVectorQuery query = new KnnFloatVectorQuery("vector", queryVector, 2);  // 查找2个最相似的文档
            QueryParser parser = new QueryParser("text", analyzer);
            Query query = parser.parse("摔倒");
            TopDocs results = searcher.search(query, 1);

            // 输出搜索结果
            System.out.println("Top matching documents:" + results.scoreDocs.length);
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document doc = searcher.storedFields().document(scoreDoc.doc);
                System.out.println("Document ID: " + doc + ", Score: " + scoreDoc.score + "," + doc.getField("text"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
