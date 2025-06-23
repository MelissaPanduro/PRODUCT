package pe.edu.vallegrande.product.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.product.model.ProductoModel;
import pe.edu.vallegrande.product.repository.ProductoRepository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Mono<ProductoModel> createProduct(ProductoModel product) {
        return productoRepository.save(product);
    }

    public Flux<ProductoModel> getAllProducts() {
        return productoRepository.findAll();
    }

    public Mono<Void> deleteProduct(Long id) {
        return productoRepository.deleteById(id);
    }

    public Mono<ProductoModel> softDeleteProduct(Long id) {
        return productoRepository.findById(id)
            .flatMap(product -> {
                product.setStatus("I");
                return productoRepository.save(product);
            });
    }

    public Mono<ProductoModel> getProductById(Long id) {
        return productoRepository.findById(id);
    }

    public Mono<ProductoModel> getById(Long id) {
        return productoRepository.findById(id);
    }

    public Mono<ProductoModel> restoreProduct(Long id) {
        return productoRepository.findByIdAndStatus(id, "I")
            .flatMap(product -> {
                product.setStatus("A");
                return productoRepository.save(product);
            });
    }

    public Mono<ProductoModel> updateProduct(Long id, ProductoModel productDetails) {
        return productoRepository.findById(id)
            .flatMap(existingProduct -> {
                existingProduct.setType(productDetails.getType());
                existingProduct.setDescription(productDetails.getDescription());
                existingProduct.setPackageWeight(productDetails.getPackageWeight());
                existingProduct.setStock(productDetails.getStock());
                existingProduct.setEntryDate(productDetails.getEntryDate());
                existingProduct.setTypeProduct(productDetails.getTypeProduct());
                existingProduct.setStatus(productDetails.getStatus());
                return productoRepository.save(existingProduct);
            });
    }

    public Mono<ProductoModel> increaseStock(Long id, int quantityAdded) {
        return productoRepository.findById(id)
            .flatMap(product -> {
                if (quantityAdded <= 0) {
                    return Mono.error(new IllegalArgumentException("La cantidad debe ser mayor que cero."));
                }

                int nuevoStock = product.getStock() + quantityAdded;
                product.setStock(nuevoStock);

                if ("I".equals(product.getStatus()) && nuevoStock > 0) {
                    product.setStatus("A");
                }

                return productoRepository.save(product);
            });
    }

    public Mono<ProductoModel> adjustStock(Long id, int quantityChange) {
        return productoRepository.findById(id)
            .flatMap(product -> {
                if (quantityChange == 0) {
                    return Mono.just(product);
                }

                int nuevoStock = product.getStock() + quantityChange;

                if (nuevoStock < 0) {
                    return Mono.error(new IllegalArgumentException("Stock insuficiente para realizar la operación."));
                }

                product.setStock(nuevoStock);

                if (nuevoStock == 0 && "A".equals(product.getStatus())) {
                    product.setStatus("I");
                } else if (nuevoStock > 0 && "I".equals(product.getStatus())) {
                    product.setStatus("A");
                }

                return productoRepository.save(product);
            });
    }

    public Flux<ProductoModel> getActiveProducts() {
        return productoRepository.findAll()
                .filter(producto -> "A".equals(producto.getStatus()));
    }

    public Mono<ProductoModel> reduceStock(Long id, int quantity) {
        if (quantity <= 0) {
            return Mono.error(new IllegalArgumentException("La cantidad a reducir debe ser mayor que cero."));
        }
        return productoRepository.findById(id)
            .flatMap(product -> {
                int nuevoStock = product.getStock() - quantity;
                if (nuevoStock < 0) {
                    return Mono.error(new IllegalArgumentException("Stock insuficiente para reducir la cantidad solicitada."));
                }
                product.setStock(nuevoStock);

                if (nuevoStock == 0 && "A".equals(product.getStatus())) {
                    product.setStatus("I");
                }

                return productoRepository.save(product);
            });
    }
}
