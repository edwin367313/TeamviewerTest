import java.io.Serializable;

/**
 * Dữ liệu cho việc truyền file
 */
public class FileTransferData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String transferType; // START, CHUNK, END, ACCEPT, REJECT
    private String fileName;
    private long fileSize;
    private byte[] fileData;
    private int chunkIndex;
    private int totalChunks;
    private String fileId; // UUID để định danh file transfer
    
    // Constructor cho START
    public FileTransferData(String transferType, String fileName, long fileSize, String fileId) {
        this.transferType = transferType;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileId = fileId;
    }
    
    // Constructor cho CHUNK
    public FileTransferData(String transferType, String fileId, byte[] fileData, 
                           int chunkIndex, int totalChunks) {
        this.transferType = transferType;
        this.fileId = fileId;
        this.fileData = fileData;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
    }
    
    // Constructor cho END, ACCEPT, REJECT
    public FileTransferData(String transferType, String fileId) {
        this.transferType = transferType;
        this.fileId = fileId;
    }
    
    public String getTransferType() {
        return transferType;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public byte[] getFileData() {
        return fileData;
    }
    
    public int getChunkIndex() {
        return chunkIndex;
    }
    
    public int getTotalChunks() {
        return totalChunks;
    }
    
    public String getFileId() {
        return fileId;
    }
    
    public String getFileSizeFormatted() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }
}
