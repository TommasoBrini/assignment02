package ex2.eventLoop.searcher;

public interface Searcher {

    int currentDepth();

    String url();

    String word();

    int countWord();

    int findUrls();
}
