package mgarzon.createbest.productcatalog;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference productsDatabase;

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;

    List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productsDatabase = FirebaseDatabase.getInstance().getReference("products");

        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        listViewProducts = findViewById(R.id.listViewProducts);
        buttonAddProduct = findViewById(R.id.addButton);

        products = new ArrayList<>();

        // Adding an onClickListener to button
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

        listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = products.get(i);
                showUpdateDeleteDialog(product.getId(), product.getProductName());
                return true;
            }
        });

        ProductList productAdapter = new ProductList(MainActivity.this, products);
        listViewProducts.setAdapter(productAdapter);

    }


    @Override
    protected void onStart() {
        super.onStart();

        productsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                products.clear();

                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                    Product product = postSnapshot.getValue(Product.class);
                    products.add(product);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void showUpdateDeleteDialog(final String productId, String productName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final EditText editTextPrice  = (EditText) dialogView.findViewById(R.id.editTextPrice);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
                if (!TextUtils.isEmpty(name)) {
                    updateProduct(productId, name, price);
                    b.dismiss();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct(productId);
                b.dismiss();
            }
        });
    }

    private void updateProduct(String id, String name, double price) {

        DatabaseReference productReference = productsDatabase.child(id);

        Product product = new Product(id, name, price);

        productReference.setValue(product);

        Toast.makeText(MainActivity.this, "Product updated.", Toast.LENGTH_LONG).show();

    }

    private void deleteProduct(String id) {

        DatabaseReference productReference = productsDatabase.child(id);

        productReference.removeValue();

        Toast.makeText(getApplicationContext(), "Product deleted.", Toast.LENGTH_LONG).show();

    }

    private void addProduct() {

        String productName = editTextName.getText().toString();
        String priceString = editTextPrice.getText().toString();

        if(!TextUtils.isEmpty(productName) && !TextUtils.isEmpty(priceString)) {

            String id = productsDatabase.push().getKey();

            double price = Double.parseDouble(priceString);

            Product product = new Product(id, productName, price);
            productsDatabase.child(id).setValue(product);

            editTextName.setText("");
            editTextPrice.setText("");

            Toast.makeText(MainActivity.this, "Product added.", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(MainActivity.this, "Please enter a name for the product.", Toast.LENGTH_LONG).show();
        }

    }
}