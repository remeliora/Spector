# Основные изменения COMMIT_2

### (13.11.2023)

На данный момент в разработке отдельного фронтэнд нет особой необходимости, но в приложение необходимо добавить REST-full API, так что пока придётся немного костыльно прописывать методы REST-запросов либо в самих контроллерах, где уже происходит обслуживание HTML-страницы, либо создавать отдельные контроллеры, для более удобного разграничения.

Приложение запускается, но происходит циклированная переадресация на /login. Также не проходит сохранение пользователя в базе данных.

### (16.11.2023)

Удалось решить проблему с циклированием. 
Как выяснилось, пропала аннотация в _MvcConfig_. 

Проблему с добавлением пользователя в БД получилось решить путём замены типа метода `findByUsername` в репозитории _UserRepository_.
До этого метод был типа Optional, что на самом деле правильно, с точки зрения чистого кода для JAVA. 
Но я не учёл того факта, что при сравнивании значения с ненулевым, Optional < User > не может быть равен null. 
Поэтому пока поменял на более простое решение:

    User findByUsername(String username);
    
    вместо

    Optional<User> findByUsername(String username);

Соответственно теперь методы в _UserService_ выглядят так:

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return user;
    }

    public boolean addUser (User user) {
        User userFromDB = userRepository.findByUsername(user.getUsername());

        if (userFromDB != null) {
            return false;
        }

        user.setRoles(Collections.singleton(Role.TECHNICIAN));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return true;
    }

Если в будущем можно будет поменять таким образом:
Использовать `isPresent()` в методе сервиса _UserService_:

    if (userFromDB.isPresent()) {
    return false;
    }

Для проверки существования пользователя в базе данных:

Вместо создания `Optional` и проверки `isPresent`, можно использовать метод `existsByUsername`, предоставляемый Spring Data JPA.

    Optional<User> userFromDB = userRepository.findByUsername(user.getUsername());
    
    if (userFromDB.isPresent()) {
    return false;
    }

Вышенаписанное необходимо будет в будущем заменить на следующий код:

    if (userRepository.existsByUsername(user.getUsername())) {
    return false;
    }

Это упростит код и сделает его более читаемым.

### (17.11.2023)

Так как с простой реализацией добавления пользователя и авторизацией разобрался, сейчас занимаюсь редактированием параметров пользователя (поменять имя, пароль, сохранить изменения, задать права пользователю).

На данный момент пока реализованы только формы показа списка пользователей и редактирование их имени и роли.
Необходимо будет добавить ограничение, для того чтобы данные формы были доступны только пользователю с ролью "инженер" (админ).
Также позже надо будет добавить проверки на исключения некоторых ошибок, всплывающие сообщения в случае ошибок при вводе данных и также поменять шифрование некоторых данных в БД, помимо паролей. 
Лучше перейти на JWT Tokens или стандартный BCrypt поставляемый вместе со SPRING SECURITY