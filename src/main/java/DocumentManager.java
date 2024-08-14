import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc.
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */

@Data
@AllArgsConstructor
public class DocumentManager {

    private List<Document> storedData;
    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if(document == null){
            throw new NullPointerException();
        }

        if(document.getId().isEmpty() || document.getId().isBlank()){
            document.setId(UUID.randomUUID().toString());
        }

        for(int i = 0; i < storedData.size(); i++){
            Document tempDoc = storedData.get(i);
            if(tempDoc.getId().equals(document.getId())){
                storedData.set(i, document);
                return document;
            }
        }

        storedData.add(document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if(request == null) {
            return Collections.emptyList();
        }

        return storedData.stream()
                .filter(doc -> checkTitlePrefixes(doc, request.getTitlePrefixes()))
                .filter(doc -> checkAuthor(doc, request.getAuthorIds()))
                .filter(doc -> checkContents(doc, request.getContainsContents()))
                .filter(doc -> checkDate(doc, request.getCreatedFrom(), request.getCreatedTo()))
                .toList();
    }

    private boolean checkDate(Document doc, Instant createdFrom, Instant createdTo) {
        Instant createDate = doc.getCreated();

        if(createdFrom != null && createDate.isBefore(createdFrom)){
            return false;
        }
        if(createdTo != null && createDate.isAfter(createdTo)){
            return false;
        }

        return true;
    }

    private boolean checkContents(Document doc, List<String> containsContents) {
        if(containsContents == null || containsContents.isEmpty()){
            return true;
        }

        return containsContents.stream()
                .anyMatch(content -> doc.getContent().contains(content));
    }

    private boolean checkAuthor(Document doc, List<String> authorIds) {
        if(authorIds == null || authorIds.isEmpty()){
            return true;
        }

        return authorIds.contains(doc.getAuthor().id);
    }

    private boolean checkTitlePrefixes(Document doc, List<String> titlePrefixes) {
        if(titlePrefixes == null || titlePrefixes.isEmpty()){
            return true;
        }

        return titlePrefixes.stream()
                .anyMatch(prefix -> doc.getTitle().startsWith(prefix));
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return storedData.stream()
                .filter(doc -> doc.getId().equals(id))
                .findFirst();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}