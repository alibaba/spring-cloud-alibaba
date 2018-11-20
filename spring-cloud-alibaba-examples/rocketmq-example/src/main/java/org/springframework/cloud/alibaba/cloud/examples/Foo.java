package org.springframework.cloud.alibaba.cloud.examples;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class Foo {

	private int id;
	private String tag;

	public Foo() {
	}

	public Foo(int id, String tag) {
		this.id = id;
		this.tag = tag;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	public String toString() {
		return "Foo{" + "id=" + id + ", tag='" + tag + '\'' + '}';
	}
}
