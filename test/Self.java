class Self {
    public static void main(String[] args) {
        System.out.println(new Inner().test());
    }
}

class Inner {
    int attr;
    public int func(int arg) {
        attr = arg;
        return 0;
    }

    public int test() {
        int ret;

        ret = this.func(5);
        attr = 10;
        if (attr > 5) {
            ret = attr;
        } else {
            ret = 222;
        }
        return ret;
    }
}