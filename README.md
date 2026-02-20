**Rebar Job Manager - Java II Final Project**

**Idea Selection:** Desktop app solving real rebar detailing business problems (job tracking, bar list management, status workflow). Directly addresses "solving an inefficiency at work".

**Interface:** Professional Swing GUI with dark theme sidebar navigation (Jobs/Add Job/Add Item/Reports/Save). Uses CardLayout for screen switching, JTable for job lists, input forms with validation.

**Tools & Techniques from Java II:**
- Collections: ArrayList<Job>, List<RebarItem> for dynamic job/item storage
- OOP Design: JobManager class orchestrates Job→List<RebarItem> domain model  
- File I/O: JSON serialization to Windows AppData folder
- Generics & Interfaces: Comparable<Job> for sorting
- Exception Handling: Input validation, file errors
- Swing Layouts: CardLayout, GridBagLayout, BoxLayout
- Deployment: jpackage creates native Windows .exe installer

**Beyond CS&141:** Requires Collections Framework, persistent storage, multi-class relationships, professional deployment.
