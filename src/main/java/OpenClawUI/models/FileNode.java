package OpenClawUI.models;

public class FileNode {
    public String path, name, type;
    public long size;

    public FileNode(String path) {
        this.path = path;
        this.name = new java.io.File(path).getName();
        this.size = new java.io.File(path).length();
        this.type = getFileType(name);
    }

    private String getFileType(String name) {
        if (name.endsWith(".pdf"))
            return "PDF";
        if (name.endsWith(".jpg") || name.endsWith(".png"))
            return "Image";
        return "File";
    }
}
