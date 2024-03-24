import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Menu extends JFrame{
    public static void main(String[] args) {
        // buat object window
        Menu window = new Menu();

        // atur ukuran window
        window.setSize(480, 560);
        // letakkan window di tengah layar
        window.setLocationRelativeTo(null);
        // isi window
        window.setContentPane(window.mainPanel);
        // ubah warna background
        window.getContentPane().setBackground(Color.white);
        // tampilkan window
        window.setVisible(true);
        // agar program ikut berhenti saat window diclose
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // index baris yang diklik
    private int selectedIndex = -1;
    // list untuk menampung semua mahasiswa
    private ArrayList<Mahasiswa> listMahasiswa;
    private Database database;

    private JPanel mainPanel;
    private JTextField nimField;
    private JTextField namaField;
    private JTable mahasiswaTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox jenisKelaminComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel nimLabel;
    private JLabel namaLabel;
    private JLabel jenisKelaminLabel;

    // constructor
    public Menu() {
        // inisialisasi listMahasiswa
        listMahasiswa = new ArrayList<>();

        //buat objek database
        database = new Database();

//        // isi listMahasiswa
//        populateList();

        // isi tabel mahasiswa
        mahasiswaTable.setModel(setTable());

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box
        String[] jenisKelaminData = {"", "Laki-laki", "Perempuan"};
        jenisKelaminComboBox.setModel(new DefaultComboBoxModel(jenisKelaminData));

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex == -1) {
                    insertData();
                } else {
                    updateData();
                }
            }
        });
        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex >= 0) {
                    deleteData();
                }
            }
        });
        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // saat tombol
                clearForm();
            }
        });
        // saat salah satu baris tabel ditekan
        mahasiswaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = mahasiswaTable.getSelectedRow();

                // simpan value textfield dan combo box
                String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
                String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex, 2).toString();
                String selectedJenisKelamin = mahasiswaTable.getModel().getValueAt(selectedIndex, 3).toString();

                // ubah isi textfield dan combo box
                nimField.setText(selectedNim);
                namaField.setText(selectedNama);
                jenisKelaminComboBox.setSelectedItem(selectedJenisKelamin);

                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");
                // tampilkan button delete
                deleteButton.setVisible(true);
            }
        });
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] column = {"No", "NIM", "Nama", "Jenis Kelamin"};

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel temp = new DefaultTableModel(null, column);

//        // isi tabel dengan listMahasiswa
//        for (int i = 0; i < listMahasiswa.size(); i++) {
//            Object[] row = new Object[4];
//            row[0] = i + 1;
//            row[1] = listMahasiswa.get(i).getNim();
//            row[2] = listMahasiswa.get(i).getNama();
//            row[3] = listMahasiswa.get(i).getJenisKelamin();
//
//            temp.addRow(row);
//        }

        try{
            ResultSet resultSet = database.selectQuery("SELECT * FROM mahasiswa");

            int i = 0;
            while (resultSet.next()){
                Object[] row = new Object[4];

                row[0] = i + 1;
                row[1] = resultSet.getString("nim");
                row[2] = resultSet.getString("nama");
                row[3] = resultSet.getString("jenis_kelamin");

                temp.addRow(row);
                i++;
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }

        return temp;
    }

    public void insertData() {
        // ambil value dari textfield dan combobox
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();

        if(nim.isEmpty() || nama.isEmpty() || jenisKelamin.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Data tidak boleh ada yang kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Kembali jika ada input yang kosong
        }

        String sqlCheck = "SELECT COUNT(*) FROM mahasiswa WHERE nim = '" + nim + "'";
        int count = 0;

        try (Statement statement = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_mahasiswa").createStatement();
             ResultSet resultSet = statement.executeQuery(sqlCheck)) {
            // Jika query mengembalikan hasil, ambil nilai COUNT(*)
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            // Tangani exception jika terjadi kesalahan saat eksekusi query
            e.printStackTrace();
        }

        // Periksa apakah NIM sudah ada di database
        if (count > 0) {
            JOptionPane.showMessageDialog(null, "NIM sudah ada dalam database!", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Kembali jika NIM sudah ada
        }

        // Tambahkan data ke dalam database
        String sqlInsert = "INSERT INTO mahasiswa VALUES (null, '" + nim + "', '" + nama + "', '" + jenisKelamin + "');";
        database.insertUpdateDeleteQuery(sqlInsert);

        // Update tabel
        mahasiswaTable.setModel(setTable());

        // Bersihkan form
        clearForm();

        // Feedback
        System.out.println("Insert berhasil!");
        JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan");
    }

    public void updateData() {
        // ambil data dari form
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();

        // ubah data mahasiswa di list
//        listMahasiswa.get(selectedIndex).setNim(nim);
//        listMahasiswa.get(selectedIndex).setNama(nama);
//        listMahasiswa.get(selectedIndex).setJenisKelamin(jenisKelamin);

        if (nim.isEmpty() || nama.isEmpty() || jenisKelamin.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Data tidak boleh ada yang kosong!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // ubah data mahasiswa di database
            String sql = "UPDATE mahasiswa SET nama='" + nama + "', jenis_kelamin='" + jenisKelamin + "' WHERE nim='" + nim + "'";
            database.insertUpdateDeleteQuery(sql);

            // update tabel
            mahasiswaTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Update Berhasil!");
            JOptionPane.showMessageDialog(null, "Data berhaasil diubah!");
        }
    }

    public void deleteData() {
        // hapus data dari list
        int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus data?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // hapus data dari list
            int selectedIndex = mahasiswaTable.getSelectedRow();
            if (selectedIndex >= 20) {
                String nim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
                String sql = "DELETE FROM mahasiswa WHERE nim = '" + nim + "'";
            database.insertUpdateDeleteQuery(sql);

            listMahasiswa.remove(selectedIndex);

            // update tabel
            mahasiswaTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Delete berhasil!");
            JOptionPane.showMessageDialog(null, "Data berhasil dihapus!");
            } else {
                JOptionPane.showMessageDialog(null, "Data dengan urutan di bawah 20 tidak dapat dihapus.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void clearForm() {
        // kosongkan semua texfield dan combo box
        nimField.setText("");
        namaField.setText("");
        jenisKelaminComboBox.setSelectedItem("");

        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");
        // sembunyikan button delete
        deleteButton.setVisible(false);
        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;
    }

//    private void populateList() {
//        listMahasiswa.add(new Mahasiswa("2203999", "Amelia Zalfa Julianti", "Perempuan"));
//        listMahasiswa.add(new Mahasiswa("2202292", "Muhammad Iqbal Fadhilah", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2202346", "Muhammad Rifky Afandi", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2210239", "Muhammad Hanif Abdillah", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2202046", "Nurainun", "Perempuan"));
//        listMahasiswa.add(new Mahasiswa("2205101", "Kelvin Julian Putra", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2200163", "Rifanny Lysara Annastasya", "Perempuan"));
//        listMahasiswa.add(new Mahasiswa("2202869", "Revana Faliha Salma", "Perempuan"));
//        listMahasiswa.add(new Mahasiswa("2209489", "Rakha Dhifiargo Hariadi", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2203142", "Roshan Syalwan Nurilham", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2200311", "Raden Rahman Ismail", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2200978", "Ratu Syahirah Khairunnisa", "Perempuan"));
//        listMahasiswa.add(new Mahasiswa("2204509", "Muhammad Fahreza Fauzan", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2205027", "Muhammad Rizki Revandi", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2203484", "Arya Aydin Margono", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2200481", "Marvel Ravindra Dioputra", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2209889", "Muhammad Fadlul Hafiizh", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2206697", "Rifa Sania", "Perempuan"));
//        listMahasiswa.add(new Mahasiswa("2207260", "Imam Chalish Rafidhul Haque", "Laki-laki"));
//        listMahasiswa.add(new Mahasiswa("2204343", "Meiva Labibah Putri", "Perempuan"));
//    }
}
