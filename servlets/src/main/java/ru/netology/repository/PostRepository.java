package ru.netology.repository;

import ru.netology.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Stub
public class PostRepository {

    private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();
    private final AtomicLong count = new AtomicLong(1);

    public List<Post> all() {
        List<Post> postList = new ArrayList<>();

        for (long k :
                posts.keySet()) {
            postList.add(posts.get(k));
        }

        return postList;
    }

    public Optional<Post> getById(long id) {
        System.out.println("ID " + id);
        return Optional.ofNullable(posts.get(id));
    }

    public Post save(Post post) {
        long id;
        if (post.getId() == 0) {
            id = count.getAndIncrement();
            post.setId(id);
            posts.put(id, post);
        } else {
            if (post.getId() > count.get()) {
                //todo отпарвить сообщение клиенту, что пост сохранен с другим id
                id = count.getAndIncrement();
                post.setId(id);
            } else {
                id = post.getId();
            }
            posts.put(id, post);
        }
        return post;
    }

    public void removeById(long id) {
        if (posts.get(id) != null) {
            posts.remove(id);
        }
    }
}
