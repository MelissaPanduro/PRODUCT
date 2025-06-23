package pe.edu.vallegrande.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.product.model.ProductoModel;
import pe.edu.vallegrande.product.service.ProductoService;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/NPH/products")
@CrossOrigin(origins = "*")
public class ProductoRest {

    @Autowired
    private ProductoService productoService;

    // Obtener todos los productos
    @GetMapping
    public Flux<ProductoModel> getAllProducts() {
        return productoService.getAllProducts();
    }

    // Obtener producto por ID
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductoModel>> getProductById(@PathVariable Long id) {
        return productoService.getProductById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Crear nuevo producto
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductoModel> createProduct(@RequestBody ProductoModel product) {
        return productoService.createProduct(product);
    }

    // Actualizar producto
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ProductoModel>> updateProduct(@PathVariable Long id, @RequestBody ProductoModel productDetails) {
        return productoService.getProductById(id)
                .flatMap(existingProduct -> {
                    existingProduct.setDescription(productDetails.getDescription());
                    existingProduct.setStock(productDetails.getStock());
                    existingProduct.setStatus(productDetails.getStatus());
                    existingProduct.setType(productDetails.getType());
                    existingProduct.setTypeProduct(productDetails.getTypeProduct());
                    existingProduct.setPackageWeight(productDetails.getPackageWeight());
                    existingProduct.setEntryDate(productDetails.getEntryDate());
                    return productoService.updateProduct(id, existingProduct);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Eliminar producto (físicamente)
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable Long id) {
        return productoService.deleteProduct(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    // Obtener productos activos
    @GetMapping("/active")
    public Flux<ProductoModel> getActiveProducts() {
        return productoService.getActiveProducts();
    }

    // Desactivar producto (eliminación lógica)
    @PutMapping("/logic/{id}")
    public Mono<ProductoModel> softDeleteProduct(@PathVariable Long id) {
        return productoService.softDeleteProduct(id);
    }

    // Restaurar producto
    @PutMapping("/restore/{id}")
    public Mono<ProductoModel> restoreProduct(@PathVariable Long id) {
        return productoService.restoreProduct(id);
    }

    // PATCH: actualizar stock y estado desde el frontend
    @PatchMapping("/{id}/stock")
    public Mono<ResponseEntity<ProductoModel>> updateStockAndStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int newStock = ((Number) body.get("stock")).intValue();
        String newStatus = (String) body.get("status");

        return productoService.getProductById(id)
                .flatMap(product -> {
                    product.setStock(newStock);
                    product.setStatus(newStatus);
                    return productoService.updateProduct(id, product);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Aumentar stock
    @PutMapping("/increase-stock/{id}")
    public Mono<ProductoModel> increaseStock(@PathVariable Long id, @RequestParam int quantity) {
        return productoService.increaseStock(id, quantity);
    }

    // Ajustar stock manualmente (+/-)
    @PutMapping("/adjust-stock/{id}")
    public Mono<ProductoModel> adjustStock(@PathVariable Long id, @RequestParam int quantityChange) {
        return productoService.adjustStock(id, quantityChange);
    }

    // Reducir stock
    @PutMapping("/reduce-stock/{id}")
    public Mono<ProductoModel> reduceStock(@PathVariable Long id, @RequestParam int quantity) {
        return productoService.reduceStock(id, quantity);
    }
}
